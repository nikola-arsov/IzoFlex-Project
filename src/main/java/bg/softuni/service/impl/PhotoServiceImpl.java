package bg.softuni.service.impl;

import bg.softuni.model.entity.Photo;
import bg.softuni.service.interf.PhotoService;
import bg.softuni.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhotoServiceImpl implements PhotoService {
    private final PhotoRepository repository;

    @Autowired
    public PhotoServiceImpl(PhotoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void deletePicture(String id) {
        this.repository.deleteById(id);
    }

    @Override
    public String getPhotoForThumbnail(String itemId) {
        return this.repository.getFirstByItem_Id(itemId).orElseGet(Photo::new).getLocation();
    }
}