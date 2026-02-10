package ci;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HistoryController.class)
public class HistoryControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean Status status;

  @Test
  public void historyPageTest() throws Exception {
    /*
     * Contract: When getCommits() returns a List of commit records the history view should be
     * returned with status OK and contain that list in its model's attributes.
     */
    when(status.getCommits())
        .thenReturn(
            Arrays.asList(new Status.CommitRecord("dummy", "pass", Arrays.asList("message"))));
    mockMvc
        .perform(get("/history"))
        .andExpect(status().isOk())
        .andExpect(view().name("history"))
        .andExpect(model().attribute("historyList", notNullValue()));
  }
}
