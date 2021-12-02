package bg.softuni.web;

import bg.softuni.service.interf.ItemService;
import bg.softuni.util.handler.RedirectHelper;
import bg.softuni.model.binding.ItemFormModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public String getAddItemPage(Model model) {
        return this.addAttributesToItemForm("add", model, "empty");
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('USER')")
    public String getEditItemPage(@PathVariable("id") String id, Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return this.addAttributesToItemForm("edit/" + id, model, id);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public String addItemPagePost(@Valid @ModelAttribute("item") ItemFormModel item, BindingResult bindingResult, Principal principal, RedirectAttributes redirectAttributes) {
        return this.handleItemPostMethods("add", item, bindingResult, principal.getName(), redirectAttributes);
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('USER')")
    public String editItemPagePost(@Valid @ModelAttribute("item") ItemFormModel item, BindingResult bindingResult, @PathVariable(name = "id") String id, Principal principal, RedirectAttributes redirectAttributes) {
        return this.handleItemPostMethods("edit/" + id, item, bindingResult, principal.getName(), redirectAttributes);
    }

    @GetMapping("/info/{id}")
    @PreAuthorize("hasRole('USER')")
    public String getInfoPage(@PathVariable("id") String id, Principal principal, Model model) {
        model.addAttribute("item", this.itemService.getItemForInfoPage(id, principal.getName()));
        return "item-info";
    }

    @PostMapping("/remove/{id}")
    @PreAuthorize("hasRole('USER')")
    public String removeItem(@PathVariable("id") String id, Principal principal, RedirectAttributes redirectAttributes) {
        this.itemService.removeItem(id, principal.getName());
        redirectAttributes.addFlashAttribute("success", "Успешно премахнахте продукта!");

        return "redirect:/users/collection";
    }

    private String addAttributesToItemForm(String url, Model model, String itemId) {
        model.addAttribute("url", url);
        if (!model.containsAttribute("item")) {
            if ("add".equals(url)) {
                model.addAttribute("item", new ItemFormModel());
            } else {
                model.addAttribute("item", this.itemService.getItemViewById(String.valueOf(model.getAttribute("username")), itemId));
            }
        }
        return "item-form";
    }

    private String handleItemPostMethods(String url, ItemFormModel item, BindingResult bindingResult, String username, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                if (url.equals("add")) {
                    this.itemService.saveItem(username, item);
                } else {
                    this.itemService.editItem(username, item);
                }
                redirectAttributes.addFlashAttribute("success", "Продуктът " + ("add".equals(url) ? "добавен" : "редактиран") + " успешно!");

                return "redirect:/users/collection";
            } catch (IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
            }
        }
        RedirectHelper.addAttributes(redirectAttributes, "item", item, bindingResult);

        return "redirect:" + ("add".equals(url) ? url : "/items/" + url);
    }
}