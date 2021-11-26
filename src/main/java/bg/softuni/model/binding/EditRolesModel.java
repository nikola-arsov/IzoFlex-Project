package bg.softuni.model.binding;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class EditRolesModel {
    private String username;
    private List<String> roles;

    public EditRolesModel() {
    }

    public EditRolesModel(String username) {
        this.username = username;
        this.roles = new ArrayList<>();
    }

    @Length(min = 2, max = 20, message = "Дължината на потребителското име трябва да е между 2 и 20 символа!")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Size(min = 1, max = 2, message = "Трябва да изберете поне една роля!")
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
