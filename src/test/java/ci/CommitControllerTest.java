package ci;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommitController.class)
public class CommitControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean Status status;

  @Test
  public void commitPageTest() throws Exception {
    /*
     * Contract: When getLatest() returns a CommitRecord the model's attributes
     * should contain a reference to that commit, and the getter should return the commit view
     * with status OK.
     */
    when(status.getLatest())
        .thenReturn(
            Optional.of(
                new Status.CommitRecord("dummy", "pass", Instant.now().toString(), "message")));

    mockMvc
        .perform(get("/commit"))
        .andExpect(status().isOk())
        .andExpect(view().name("commit"))
        .andExpect(model().attributeExists("latestCommit"))
        .andExpect(model().attribute("latestCommit", notNullValue()));
  }
}
