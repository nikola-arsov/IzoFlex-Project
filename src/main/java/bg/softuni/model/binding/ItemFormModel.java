package bg.softuni.model.binding;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class ItemFormModel {
    private String id;
    private String name;
    private String category;
    private String description;
    private List<MultipartFile> images;

    public ItemFormModel() {
    }

    public ItemFormModel(String id, String name, String category, String description, List<MultipartFile> images) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.images = images;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Length(min = 2, max = 30, message = "Дължината на името трябва да е между 2 и 30 символа.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotBlank(message = "Категорията не може да е празна.")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Length(min = 10, message = "Описанието трябва да е поне 10 символа.")
    @NotBlank( message = "Описанието не може да бъде празно!")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Size(min = 1,max = 3,message = "Трябва да изберте 1 - 3 снимки.")
    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }
}
