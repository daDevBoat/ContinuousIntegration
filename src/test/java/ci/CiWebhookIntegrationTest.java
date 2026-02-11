package ci;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.tomcat.util.buf.HexUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

// Overwrites any properties file SpringBoot might have access too
@SpringBootTest(
    properties = {
      "sharedKey=test-secret",
      "git.repoName=daDevBoat/ContinuousIntegration",
      "ci.repoSsh=fake-ssh",
      "ci.repoID=ContinuousIntegration",
      "server.auth=fake-token",
      "local.url=http://localhost:1234"
    })
@ActiveProfiles("test")
@AutoConfigureMockMvc

/**
 * Integration tests: Use a mock HTTP post to trigger the CI workflow (CIWebhookController and
 * CIService) Uses mock values to overwrite any properties files that might exist. Uses mock
 * function returns where the results requires a special key / permissions, since these functions
 * are tested independently in their own Unit Tests.
 *
 * <p>Evaluates if the send HTTP codes are as expected.
 */
class CiWebhookIntegrationTest {
  @TempDir Path temp;

  // SECRET needs to match the sharedKey set in the properties at the top of the file
  private static final String SECRET = "test-secret";
  private static final String REPO_ID = "ContinuousIntegration";

  @Autowired MockMvc mockMvc;
  @Autowired CiService ciService;

  private final ObjectMapper om = new ObjectMapper();

  /**
   * enforces the runBuild in CiService to be executed in synchron mode, otherwise we cant get the
   * response
   */
  @TestConfiguration
  static class SyncAsyncConfig {
    @Bean(name = "taskExecutor")
    TaskExecutor taskExecutor() {
      return new SyncTaskExecutor();
    }
  }

  /**
   * Function from Validation The function HMAC:s a message, in bytes, using SHA256 and the
   * sharedKey. The answer is in hexadecimals (string).
   *
   * @param sharedKey the shared key between Github and our program
   * @param payloadBody the body sent by the webhook
   * @return the body HMAC:ed with SHA256 using sharedKey
   * @throws IllegalArgumentException if payloadBody or sharedKey is null
   * @throws NoSuchAlgorithmException if Mac.getInstance can't find the HmacSHA256 algorithm
   * @throws UnsupportedEncodingException if sharedKey.getBytes can't find "UTF-8" encoding
   * @throws InvalidKeyException if the secretKey is invalid for initializing sha256HMAC
   */
  private static String computeHMACWithSHA256(String sharedKey, byte[] payloadBody)
      throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
    if (payloadBody == null || payloadBody.length == 0 || sharedKey == null) {
      throw new IllegalArgumentException("payloadBody was empty or null.");
    }
    Mac sha256HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKey = new SecretKeySpec(sharedKey.getBytes("UTF-8"), "HmacSHA256");

    sha256HMAC.init(secretKey);
    byte[] result = sha256HMAC.doFinal(payloadBody);
    return HexUtils.toHexString(result);
  }

  /**
   * @param sha the sha to be used in the payload
   * @return A JSON payload containing the sha and a few additional necessary values for this test
   * @throws Exception when the JSON object can not be created as anticipated
   */
  private byte[] buildPayload(String sha) throws Exception {
    String json =
        """
        {
          "after": "%s",
          "repository": {
            "full_name": "daDevBoat/ContinuousIntegration",
            "name": "ContinuousIntegration",
            "owner": { "name": "daDevBoat" }
          }
        }
        """
            .formatted(sha);

    return om.readTree(json).toString().getBytes(StandardCharsets.UTF_8);
  }

  @Test
  @Disabled
  void successfulBuild_postsPendingThenSuccess() throws Exception {
    /**
     * Contract: The CI server replies first with Code 202 when it receives a HTTP post request and,
     * iff all checks within the runBuild function pass, updates the status 200 "success".
     */

    // Create the repo so it passes checks
    Files.createDirectories(temp.resolve(REPO_ID));

    // sets value repoParentDir in ciService to temp
    ReflectionTestUtils.setField(ciService, "repoParentDir", temp.toString());

    // Fake compilation success - when the compile function is caled always return success as true
    CompilationService compilationMock = mock(CompilationService.class);
    when(compilationMock.compile(any()))
        .thenReturn(new CompilationService.CompilationResult(true, List.of("ok"), 0));

    // Use the mock compilationService class
    ReflectionTestUtils.setField(ciService, "compilationService", compilationMock);

    // Setup a fake RepoSetup class
    try (MockedStatic<RepoSetup> repoSetup = mockStatic(RepoSetup.class)) {

      // Make sure none of the functions in RepoSetup returns anything (set all to null)
      repoSetup.when(() -> RepoSetup.createDir(anyString())).thenAnswer(i -> null);
      repoSetup
          .when(() -> RepoSetup.cloneRepo(anyString(), anyString(), anyString()))
          .thenAnswer(i -> null);
      repoSetup
          .when(() -> RepoSetup.updateRepo(anyString(), anyString(), anyString()))
          .thenAnswer(i -> null);

      // Setup a fake GitHubAPI Handler
      try (MockedConstruction<GithubAPIHandler> apiCons =
          mockConstruction(
              GithubAPIHandler.class,
              (mock, ctx) -> {
                doNothing().when(mock).sendPost(anyString(), anyString(), anyString(), anyString());
              })) {

        // Building the fake payload
        String sha = "0123456789abcdef0123456789abcdef01234567";
        byte[] payloadBody = buildPayload(sha);
        String sig = "sha256=" + computeHMACWithSHA256(SECRET, payloadBody);

        // Building a fake http post
        mockMvc
            .perform(
                post("/webhook/github")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-GitHub-Event", "push")
                    .header("X-Hub-Signature-256", sig)
                    .content(payloadBody))
            .andExpect(status().isAccepted());

        // Check if the GitHubAPIHandler would have send the correct status
        GithubAPIHandler handler = apiCons.constructed().get(0);
        var inOrder = inOrder(handler);
        inOrder.verify(handler).sendPost(anyString(), anyString(), eq("pending"), anyString());
        inOrder.verify(handler).sendPost(anyString(), anyString(), eq("success"), anyString());
      }
    }
  }

  @Test
  void failedBuild_postsPendingThenFailure() throws Exception {
    /**
     * Contract: The CI server replies first with Code 202 when it receives a HTTP post request and,
     * iff all the build fails, updates the status to "failure".
     */

    // Create the repo so it passes checks
    Files.createDirectories(temp.resolve(REPO_ID));

    // sets value repoParentDir in ciService to temp
    ReflectionTestUtils.setField(ciService, "repoParentDir", temp.toString());

    // Fake compilation success - when the compile function is caled always return success as true
    CompilationService compilationMock = mock(CompilationService.class);
    when(compilationMock.compile(any()))
        .thenReturn(new CompilationService.CompilationResult(false, List.of("fail"), 1));

    // Use the mock compilationService class
    ReflectionTestUtils.setField(ciService, "compilationService", compilationMock);

    // Setup a fake RepoSetup class
    try (MockedStatic<RepoSetup> repoSetup = mockStatic(RepoSetup.class)) {

      // Make sure none of the functions in RepoSetup returns anything (set all to null)
      repoSetup.when(() -> RepoSetup.createDir(anyString())).thenAnswer(i -> null);
      repoSetup
          .when(() -> RepoSetup.cloneRepo(anyString(), anyString(), anyString()))
          .thenAnswer(i -> null);
      repoSetup
          .when(() -> RepoSetup.updateRepo(anyString(), anyString(), anyString()))
          .thenAnswer(i -> null);

      // Setup a fake GitHubAPI Handler
      try (MockedConstruction<GithubAPIHandler> apiCons =
          mockConstruction(
              GithubAPIHandler.class,
              (mock, ctx) -> {
                doNothing().when(mock).sendPost(anyString(), anyString(), anyString(), anyString());
              })) {

        // Building the fake payload
        String sha = "0123456789abcdef0123456789abcdef01234567";
        byte[] payloadBody = buildPayload(sha);
        String sig = "sha256=" + computeHMACWithSHA256(SECRET, payloadBody);

        // Building a fake http post
        mockMvc
            .perform(
                post("/webhook/github")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-GitHub-Event", "push")
                    .header("X-Hub-Signature-256", sig)
                    .content(payloadBody))
            .andExpect(status().isAccepted());

        // Check if the GitHubAPIHandler would have send the correct status
        GithubAPIHandler handler = apiCons.constructed().get(0);
        var inOrder = inOrder(handler);
        inOrder.verify(handler).sendPost(anyString(), anyString(), eq("pending"), anyString());
        inOrder.verify(handler).sendPost(anyString(), anyString(), eq("failure"), anyString());
      }
    }
  }
}
