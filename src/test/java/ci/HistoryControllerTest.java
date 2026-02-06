package ci;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HistoryController.class)
public class HistoryControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  public void historyPageTest() throws Exception {
    /*
     * Contract: Attributes should contain a non-null list with the commits.
     */
    mockMvc
        .perform(get("/history"))
        .andExpect(status().isOk())
        .andExpect(view().name("history"))
        .andExpect(model().attribute("historyList", notNullValue()));
  }
}
