package ci;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
// For checking status codes
// Required for Spring Boot 3+
import reactor.core.publisher.Mono;

public class GithubAPIHandler {
  private final JsonNode payload;

  GithubAPIHandler(JsonNode payload) {
    this.payload = payload;
  }

  /**
   * Method to send commit status POST request
   *
   * @param token Github auth token saved in application-local.properties
   * @param targetUrl Target url made by ngrok saved in application-local.properties
   * @param state Can be one of: error, failure, pending, success
   * @param desc The text to be displayed in the status
   */
  public void sendPost(String token, String targetUrl, String state, String desc)
      throws WebClientResponseException {

    /* The branch was deleted no status should be sent */
    if (payload.get("after").asText().equals("0000000000000000000000000000000000000000")) {
      return;
    }

    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("state", state);
    bodyMap.put("description", desc);
    bodyMap.put("context", "ci/server");
    bodyMap.put("target_url", targetUrl + "/history");

    // System.out.println("SHA: " + payload.get("after").asText());
    // System.out.println("Auth: " + token);
    // System.out.println(bodyMap.toString());

    WebClient client = WebClient.create("");

    Mono<String> responseMono =
        client
            .post()
            .uri(
                "https://api.github.com/repos/{owner}/{repo}/statuses/{sha}",
                payload.get("repository").get("owner").get("name").asText(), // daDevBoat
                payload.get("repository").get("name").asText(),
                payload.get("after").asText())
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(bodyMap)
            .retrieve()
            .bodyToMono(String.class);

    try {
      String response = responseMono.block();
      System.out.println("Response: " + response);
    } catch (WebClientResponseException e) { // Wrong formating, sha, etc. 4xx and 5xx errors
      System.err.println("API Call Failed!");
      System.err.println("Status Code: " + e.getStatusCode());
      System.err.println("Error Body: " + e.getResponseBodyAsString());
      throw e;
    } catch (Exception e) { // Something else went wrong (internet connection etc)
      System.err.println(e.getMessage());
      throw e;
    }
  }
}
