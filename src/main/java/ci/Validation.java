package ci;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.tomcat.util.buf.HexUtils;

/**
 * Validation class for authentication and validation of 
 * Github webhook requests
 * 
 * <p>This class provides functions to verify that incoming
 * webhook event are valid by checking the event type,
 * repository name, and HMAC-SHA256 signature. 
 */
public class Validation {

  /**
   * Used to validate that the event type from the GitHub WebHook is "push"
   *
   * @param event The event from the GitHub Webhook
   * @return The result of the push check validation
   */
  public static boolean validatePushEvent(String event) {
    return event.equals("push");
  }

  /**
   * Used to validate that the payload is from the correct repo
   *
   * @param payload the JSON payload of the GitHub webhook
   * @param expectedRepoName the expected repository name
   * @return The result of the repo check validation
   */
  public static boolean validateRepoName(JsonNode payload, String expectedRepoName) {
    String repoName = payload.get("repository").get("full_name").asText();
    String cleanedExpected = expectedRepoName.replace("\"", "").trim();
    return repoName.trim().equals(cleanedExpected);
  }

  /**
   * The function validates the given signature by comparing it against the result of HMAC:ing the
   * payloadBody with SHA256 and the sharedKey.
   *
   * @param sharedKey the shared key between Github and our program
   * @param payloadBody the body sent by the webhook
   * @param signature the signature sent by the webhook
   * @return true if the signature and computed HMAC is equal
   * @throws Exception if the HMAC computation failed due to bad input or HMAC computation Java
   *     issues
   */
  public static boolean validateSignature(String sharedKey, byte[] payloadBody, String signature)
      throws Exception {
    return ("sha256=" + computeHMACWithSHA256(sharedKey, payloadBody)).equals(signature);
  }

  /**
   * The function HMAC:s a message, in bytes, using SHA256 and the sharedKey. The answer is in
   * hexadecimals (string).
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
}
