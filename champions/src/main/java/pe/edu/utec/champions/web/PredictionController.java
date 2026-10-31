package pe.edu.utec.champions.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.utec.champions.service.PredictionService;

@Controller
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/predictions")
    public String list(Model model) {
        model.addAttribute("predictions", predictionService.listAll());
        return "predictions";
    }
}
