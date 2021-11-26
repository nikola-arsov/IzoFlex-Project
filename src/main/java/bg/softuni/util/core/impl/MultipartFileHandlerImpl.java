package bg.softuni.util.core.impl;

import bg.softuni.util.core.MultipartFileHandler;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class MultipartFileHandlerImpl implements MultipartFileHandler {
    private final String PATH = "uploads";

    public MultipartFileHandlerImpl() {
    }

    @Override
    public String saveFile(String username, MultipartFile file) {
        if (!Pattern.matches(".+\\.(jpg|png)", file.getOriginalFilename())) {
            throw new IllegalStateException("Please enter photo in [.jpg / .png] format.");
        }
        String fileName = file.getOriginalFilename();
        String url = String.format("%s/%s_%s_%s", this.PATH
                , username, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        .format(LocalDateTime.now()), fileName);
        try {
            File uploadDirCheck = new File(this.PATH);

            if (!uploadDirCheck.exists()) {
                uploadDirCheck.mkdirs();
            }
            File temp = new File(url);
            FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(temp));

            return "/" + url;
        } catch (IOException e) {
            throw new IllegalStateException("There was an error uploading your file, please try again later.");
        }
    }
}
