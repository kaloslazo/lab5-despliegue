package pe.edu.utec.champions.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utec.champions.model.Match;
import pe.edu.utec.champions.model.Outcome;
import pe.edu.utec.champions.service.InvalidPredictionException;
import pe.edu.utec.champions.service.MatchService;
import pe.edu.utec.champions.service.PredictionService;

@Controller
public class MatchController {

    private final MatchService matchService;
    private final PredictionService predictionService;

    public MatchController(MatchService matchService, PredictionService predictionService) {
        this.matchService = matchService;
        this.predictionService = predictionService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/matches";
    }

    @GetMapping("/matches")
    public String list(@RequestParam(required = false) String stage, Model model) {
        model.addAttribute("matches", matchService.list(stage));
        model.addAttribute("stages", matchService.stages());
        model.addAttribute("stage", stage);
        model.addAttribute("source", matchService.sourceName());
        return "matches";
    }

    @GetMapping("/matches/{id}")
    public String detail(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PredictionForm());
        }
        fillDetail(id, model);
        return "match";
    }

    @PostMapping("/matches/{id}/predictions")
    public String predict(@PathVariable Long id, @Valid @ModelAttribute("form") PredictionForm form,
                          BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            fillDetail(id, model);
            return "match";
        }
        try {
            predictionService.register(id, form.getAuthor(), form.getOutcome(),
                    form.getHomeGoals(), form.getAwayGoals());
        } catch (InvalidPredictionException e) {
            model.addAttribute("error", e.getMessage());
            fillDetail(id, model);
            return "match";
        }
        redirect.addFlashAttribute("ok", "Prediccion registrada");
        return "redirect:/matches/" + id;
    }

    private void fillDetail(Long id, Model model) {
        Match match = matchService.get(id);
        model.addAttribute("match", match);
        model.addAttribute("outcomes", Outcome.values());
        model.addAttribute("predictions", predictionService.listByMatch(id));
    }
}
