package bg.softuni.model.binding;

import org.hibernate.validator.constraints.Length;

public class AddCommentModel {
    private String content;

    public AddCommentModel() {
    }

    public AddCommentModel(String content) {
        this.content = content;
    }

    @Length(min = 1,max = 150,message = "Дължината на коментара трябва да е между 1 и 150 символа!")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
