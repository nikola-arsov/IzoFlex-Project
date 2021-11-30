package bg.softuni.web;

import bg.softuni.model.binding.EditProfileModel;
import bg.softuni.model.binding.EditRolesModel;
import bg.softuni.model.binding.MoneyTransactionModel;
import bg.softuni.model.binding.RegisterUserModel;
import bg.softuni.model.view.ProfileView;
import bg.softuni.util.handler.LoginFailureHandler;
import bg.softuni.util.handler.RedirectHelper;
import bg.softuni.service.interf.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getLoginPage(Principal principal, Model model, RedirectAttributes redirectAttributes) {
        if (checkIfAuthenticated("Login", principal, redirectAttributes)) return "redirect:/";

        if (model.getAttribute(LoginFailureHandler.USERNAME) != null) {
            model.addAttribute("username", model.getAttribute(LoginFailureHandler.USERNAME));
            model.addAttribute("error", model.getAttribute(LoginFailureHandler.ERROR));
        } else {
            model.addAttribute("username", "");
        }
        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage(Principal principal, Model model, RedirectAttributes redirectAttributes) {
        if (checkIfAuthenticated("Register", principal, redirectAttributes)) return "redirect:/";

        if (model.getAttribute("userModel") == null) {
            model.addAttribute("userModel", new RegisterUserModel());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerPagePost(@Valid @ModelAttribute("userModel") RegisterUserModel userModel, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                this.userService.registerUser(userModel);
                redirectAttributes.addFlashAttribute("success", "Вашата регистрация бе направена успешно!");

                return "redirect:login";
            } catch (IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
            }
        }
        RedirectHelper.addAttributes(redirectAttributes, "userModel", userModel, bindingResult);

        return "redirect:register";
    }

    @GetMapping("/deposit")
    @PreAuthorize("hasRole('USER')")
    public String getDepositPage(Model model) {
        return addAttributesToMoneyForm("deposit", model);
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('USER')")
    public String depositPagePost(@Valid @ModelAttribute("amountModel") MoneyTransactionModel amountModel, BindingResult bindingResult, Principal principal, RedirectAttributes redirectAttributes) {
        return this.handleMoneyTransactionsForPostMethod("deposit", amountModel, bindingResult, principal.getName(), redirectAttributes);
    }

    @GetMapping("/withdraw")
    @PreAuthorize("hasRole('USER')")
    public String getWithdrawPage(Model model) {
        return addAttributesToMoneyForm("withdraw", model);
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('USER')")
    public String withdrawPagePost(@Valid @ModelAttribute("amountModel") MoneyTransactionModel amountModel, BindingResult bindingResult, Principal principal, RedirectAttributes redirectAttributes) {
        return this.handleMoneyTransactionsForPostMethod("withdraw", amountModel, bindingResult, principal.getName(), redirectAttributes);
    }

    @Transactional
    @GetMapping("/collection")
    @PreAuthorize("hasRole('USER')")
    public String getCollectionPage(Model model, Principal principal) {
        if (model.getAttribute("items") == null) {
            model.addAttribute("items", this.userService.getCollection(principal.getName()));
        }
        return "collection";
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String getProfilePage(Model model, Principal principal) {
        model.addAttribute("user", this.userService.getProfileView(principal.getName()));
        return "profile";
    }

    @GetMapping("/profile/edit")
    @PreAuthorize("hasRole('USER')")
    public String getEditProfilePage(Model model, Principal principal) {
        ProfileView view = this.userService.getProfileView(principal.getName());

        if (!model.containsAttribute("editModel")) {
            model.addAttribute("editModel", new EditProfileModel(view.getFirstName(), view.getLastName()));
        }
        return "edit-profile";
    }

    @PostMapping("/profile/edit")
    @PreAuthorize("hasRole('USER')")
    public String profilePageEditPost(@Valid @ModelAttribute("editModel") EditProfileModel model, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal) {
        if (!bindingResult.hasErrors()) {
            try {
                this.userService.updateProfileInfo(principal.getName(), model);
                redirectAttributes.addFlashAttribute("success", "Успешно направихте промяна на профила си!");

                return "redirect:/";
            } catch (IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
            }
        }
        RedirectHelper.addAttributes(redirectAttributes, "editModel", model, bindingResult);

        return "redirect:/users/profile/edit";
    }

    @GetMapping("/roles/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String getEditRolesPage(Model model) {
        if (model.getAttribute("model") == null) {
            model.addAttribute("model", new EditRolesModel());
        }
        return "edit-role";
    }

    @PostMapping("/roles/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editRolesPagePost(@Valid @ModelAttribute("model") EditRolesModel model, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                this.userService.changeRoles(model);
                redirectAttributes.addFlashAttribute("success", "Правата на потребителя " + model.getUsername() + " бяха сменени успешно!");

                return "redirect:/";
            } catch (IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
            }
        }
        RedirectHelper.addAttributes(redirectAttributes, "model", model, bindingResult);

        return "redirect:edit";
    }

    private boolean checkIfAuthenticated(String page, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal != null) {
            redirectAttributes.addFlashAttribute("error", "Нямате достъп до " + page + " страница!");
            return true;
        }
        return false;
    }

    private String addAttributesToMoneyForm(String url, Model model) {
        model.addAttribute("url", url);

        if (!model.containsAttribute("amountModel")) {
            model.addAttribute("amountModel", new MoneyTransactionModel());
        }
        return "money-form";
    }

    private String handleMoneyTransactionsForPostMethod(String url, MoneyTransactionModel amountModel, BindingResult bindingResult, String username, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                if (("deposit").equals(url)) {
                    this.userService.deposit(username, amountModel);
                } else {
                    this.userService.withdraw(username, amountModel);
                }
                redirectAttributes.addFlashAttribute("success", String.format("Вие %s %s евро успешно!"
                        , ("deposit").equals(url) ? "депозирахте" : "прехвърлихте"
                        , amountModel.getAmount()));

                return "redirect:/";
            } catch (IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
            }
        }
        RedirectHelper.addAttributes(redirectAttributes, "amountModel", amountModel, bindingResult);

        return "redirect:" + url;
    }
}