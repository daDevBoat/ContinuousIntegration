package ci;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HistoryController is a Spring MVC controller for
 * handling commit history requests
 */
@Controller
public class HistoryController {

  private final Status status;

  /**
   * Constructs a HistoryController with the specified
   * Status service.
   * 
   * @param status the Status service used to retrieve the
   * commit history
   */
  public HistoryController(Status status) {
    this.status = status;
  }

  /**
   * Mapping for the /history url, returns a page with a list of all commits.
   *
   * @param model attributes to be sent to the view
   * @return view name
   */
  @GetMapping("/history")
  public String history(Model model) {
    model.addAttribute("historyList", status.getCommits());
    return "history";
  }
}
