package bg.softuni.model.binding;

import bg.softuni.annotation.MatchFields;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@MatchFields(first = "password",second = "repeatPassword",message = "Паролите не съвпадат!")
public class RegisterUserModel {
    private String username;
    private String password;
    private String repeatPassword;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;

    public RegisterUserModel() {
    }

    public RegisterUserModel(String username, String password, String repeatPassword, String firstName, String lastName, String email, String gender) {
        this.username = username;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
    }

    @Length(min = 3, max = 20, message = "Потребителското име трябава да е от 3 до 20 символа!")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Length(min = 6, max = 15, message = "Паролата трябава да е от 6 до 15 символа!")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Length(min = 2, max = 20, message = "Собственото име трябва да е от 2 до 20 символа!")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Length(min = 2, max = 20, message = "Фамилното име трябва да е от 2 до 20 символа!")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @NotBlank(message = "Електронната поща не може да е празна!")
    @Email(message = "Въведете валидна електронна поща!")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotBlank(message = "Пола не може да бъде празен!")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Length(min = 5, max = 15, message = "Потвърдената парола трябва да е от 5 до 15 символа!")
    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}
