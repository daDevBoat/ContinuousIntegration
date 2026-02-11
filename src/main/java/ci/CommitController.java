package ci;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * CommitController is a Spring MVC controller for handling
 * commit detail requests
 */
@Controller
public class CommitController {

  private final Status status;

  /** 
   * Constructs a CommitController with the specified Status
   * service
   * 
   * @param status the Status service used to retrieve the 
   * commit information
   */
  public CommitController(Status status) {
    this.status = status;
  }

  /**
   * Mapping for the /commit url, returns information about the latest commit if no commit was
   * specified.
   *
   * @param sha commit to be checked out (if entered)
   * @param model attributes to be sent to the view
   * @return view name
   */
  @GetMapping({"/commit", "/commit/{sha}"})
  public String commit(@PathVariable(value = "sha", required = false) String sha, Model model) {
    if (sha == null) {
      model.addAttribute("latestCommit", status.getLatest().orElse(null));
    } else {
      model.addAttribute("latestCommit", status.getCommitsMap().get(sha));
    }
    return "commit";
  }
}
