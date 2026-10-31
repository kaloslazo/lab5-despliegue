package pe.edu.utec.marketing.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utec.marketing.model.Campaign;
import pe.edu.utec.marketing.model.Network;
import pe.edu.utec.marketing.service.CampaignException;
import pe.edu.utec.marketing.service.CampaignService;

import java.io.IOException;
import java.util.function.Supplier;

@Controller
public class CampaignController {

    private final CampaignService service;

    public CampaignController(CampaignService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/campaigns";
    }

    @GetMapping("/campaigns")
    public String list(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CampaignForm());
        }
        fillList(model);
        return "campaigns";
    }

    @PostMapping("/campaigns")
    public String create(@Valid @ModelAttribute("form") CampaignForm form, BindingResult result,
                         @RequestParam("photo") MultipartFile photo, Model model) throws IOException {
        if (result.hasErrors()) {
            fillList(model);
            return "campaigns";
        }
        try {
            Campaign c = service.create(form.getBusinessName(), form.getDescription(), form.getNetwork(),
                    photo.getContentType(), photo.getBytes());
            return "redirect:/campaigns/" + c.getId();
        } catch (CampaignException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            fillList(model);
            return "campaigns";
        }
    }

    @GetMapping("/campaigns/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("c", service.get(id));
        return "campaign";
    }

    @PostMapping("/campaigns/{id}/choose")
    public String choose(@PathVariable Long id, @RequestParam Long scriptId, RedirectAttributes redirect) {
        return step(id, redirect, "Guion seleccionado", () -> service.choose(id, scriptId));
    }

    @PostMapping("/campaigns/{id}/generate")
    public String generate(@PathVariable Long id, RedirectAttributes redirect) {
        return step(id, redirect, "Se generaron 3 fotos y el video", () -> service.generateMedia(id));
    }

    @PostMapping("/campaigns/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes redirect) {
        return step(id, redirect, "Publicado", () -> service.publish(id));
    }

    @GetMapping("/campaigns/{id}/preview")
    public String preview(@PathVariable Long id, Model model) {
        model.addAttribute("c", service.get(id));
        return "preview";
    }

    private String step(Long id, RedirectAttributes redirect, String okMessage, Supplier<Campaign> action) {
        try {
            action.get();
            redirect.addFlashAttribute("ok", okMessage);
        } catch (CampaignException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/campaigns/" + id;
    }

    private void fillList(Model model) {
        model.addAttribute("campaigns", service.list());
        model.addAttribute("networks", Network.values());
        model.addAttribute("generator", service.generatorName());
    }
}
