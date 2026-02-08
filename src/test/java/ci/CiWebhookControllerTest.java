package ci;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CiWebhookController.class)
public class CiWebhookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LatestCommitStatusStore latestCommitStatusStore;

  /**
   * Contract: Given a running application with CiWebhookController configured, when a GET request
   * is made to the root path "/", then the response status should be 200 OK and the response body
   * should contain the exact message "Server is running successfully".
   */
  @Test
  public void testHomePage() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string("Server is running successfully"));
  }

  @Test
  public void testGithubWebhookInvalidEventReturnsBadRequest() throws Exception {
    /**
     * Contract (false test case): Given a running application with CiWebhookController configured,
     * when a POST request is made to "/webhook/github" with an invalid X-GitHub-Event header, then
     * the response status should be 400 Bad Request.
     */
    mockMvc
        .perform(
            post("/webhook/github")
                .header(
                    "X-GitHub-Event",
                    "pull_request") // invalid because validatePushEvent expects "push"
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }
}
