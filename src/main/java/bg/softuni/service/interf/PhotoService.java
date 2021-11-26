package bg.softuni.service.interf;

public interface PhotoService {
    void deletePicture(String id);

    String getPhotoForThumbnail(String itemId);
}
