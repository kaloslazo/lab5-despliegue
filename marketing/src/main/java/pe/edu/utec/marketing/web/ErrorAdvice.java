package pe.edu.utec.marketing.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utec.marketing.service.NotFoundException;

@ControllerAdvice
public class ErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(NotFoundException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "not-found";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooBig(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "La foto pesa demasiado (max 10 MB)");
        return "redirect:/campaigns";
    }
}
