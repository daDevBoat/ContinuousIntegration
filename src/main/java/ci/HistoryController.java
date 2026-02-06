package ci;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HistoryController {

  /**
   * Mapping for the /history url, returns a page with a list of all commits.
   *
   * @param model attributes to be sent to the view
   * @return view name
   */
  @GetMapping("/history")
  public String history(Model model) {
    List<HashMap<String, String>> historyList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      HashMap<String, String> dummy = new HashMap<>();
      dummy.put("sha", String.valueOf(i));
      dummy.put("build-date", "2026-02-05 14:30");
      dummy.put("build-logs", "Build passed");
      historyList.add(dummy);
    }
    model.addAttribute("historyList", historyList);
    return "history";
  }
}
