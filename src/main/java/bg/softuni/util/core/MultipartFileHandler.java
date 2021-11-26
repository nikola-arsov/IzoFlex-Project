package bg.softuni.util.core;

import org.springframework.web.multipart.MultipartFile;

public interface MultipartFileHandler {
    String saveFile(String username,MultipartFile file);
}
