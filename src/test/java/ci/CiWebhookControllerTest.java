package ci;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CiWebhookController.class)
public class CiWebhookControllerTest {

  @Autowired private MockMvc mockMvc;

  /**
   * Contract: Given a running application with CiWebhookController configured, when a GET request
   * is made to the root path "/", then the response status should be 200 OK and the response body
   * should contain the message "Server is running successfully".
   */
  @Test
  public void testHomePage() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string("Server is running successfully"));
  }
}
