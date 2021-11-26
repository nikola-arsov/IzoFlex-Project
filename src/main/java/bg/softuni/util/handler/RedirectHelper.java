package bg.softuni.util.handler;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class RedirectHelper {
    public static <T> void addAttributes(RedirectAttributes attributes, String modelName, T model, BindingResult bindingResult) {
        attributes.addFlashAttribute(modelName, model);
        attributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + modelName, bindingResult);
    }
}
