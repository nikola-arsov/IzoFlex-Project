package bg.softuni.config;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({UsernameNotFoundException.class,IllegalArgumentException.class})
    public String handleIllegalArgumentExceptions(RuntimeException ex , RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("flag", ex.getMessage());
        return "redirect:/error";
    }
}
