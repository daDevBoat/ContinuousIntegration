package ci;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommitController.class)
public class CommitControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  public void commitPageTest() throws Exception {
    /*
     * Contract: Attributes should contain a map with commit info.
     */
    mockMvc
        .perform(get("/commit"))
        .andExpect(status().isOk())
        .andExpect(view().name("commit"))
        .andExpect(model().attribute("commitInfo", hasKey("sha")))
        .andExpect(model().attribute("commitInfo", hasKey("build-date")))
        .andExpect(model().attribute("commitInfo", hasKey("build-logs")));
  }
}
