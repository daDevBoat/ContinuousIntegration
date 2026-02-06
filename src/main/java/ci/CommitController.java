package ci;

import ci.LatestCommitStatusStore.LatestStatus;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CommitController {

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
    // if (sha == null) {
    Optional<LatestStatus> latestCommit = ci.CiWebhookController.statusStore.get();
    if (latestCommit.isPresent()) {
      LatestStatus latestStatus = latestCommit.get();
      Instant t = latestStatus.time();
      ZoneId zone = ZoneId.systemDefault();
      String formatted =
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zone).format(t);
      model.addAttribute("latestCommitTime", formatted);
    }
    model.addAttribute("latestCommit", latestCommit.orElse(null));
    return "commit";
    // }
    // HashMap<String, String> info = new HashMap<String, String>(); // <nameOfAttribute, value>
    // setup
    // info.put("sha", sha);
    // info.put("build-date", "2026-02-05 14:30");
    // info.put("build-logs", "Build passed");
    // model.addAttribute("commitInfo", info);
    // return "commit";
  }
}
