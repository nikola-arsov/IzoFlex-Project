package bg.softuni.model.binding;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Size;
import java.util.List;

public class EditProfileModel {
    private String firstName;
    private String lastName;
    private MultipartFile images;

    public EditProfileModel() {
    }

    public EditProfileModel(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Length(min = 2, max = 20, message = "Дължината на собственото име трябва да е между 2 и 20 символа!")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Length(min = 2, max = 20, message = "Дължината на фамилното име трябва да е между 2 и 20 символа!")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public MultipartFile getImages() {
        return images;
    }

    public void setImages(MultipartFile images) {
        this.images = images;
    }
}
