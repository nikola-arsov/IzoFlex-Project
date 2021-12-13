package bg.softuni;

import bg.softuni.model.entity.Item;
import bg.softuni.model.entity.Photo;
import bg.softuni.repository.PhotoRepository;
import bg.softuni.service.impl.PhotoServiceImpl;
import bg.softuni.service.interf.PhotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PhotoServiceTest {
    @Captor
    private ArgumentCaptor<String> captor;
    @Mock
    private PhotoRepository photoRepository;
    private PhotoService photoService;


    @BeforeEach
    public void init() {
        this.photoService = new PhotoServiceImpl(photoRepository);
    }

    @Test
    public void testDeletePicture() {
        this.photoService.deletePicture("test");
        verify(photoRepository).deleteById(captor.capture());

        assertEquals("test", captor.getValue());
    }

    @Test
    public void testGetPhotoForThumbnail() {
        when(photoRepository.getFirstByItem_Id(anyString())).thenReturn(Optional.of(new Photo("test", new Item())));
        assertEquals("test", photoService.getPhotoForThumbnail("some id"));
        when(photoRepository.getFirstByItem_Id(anyString())).thenReturn(Optional.empty());
        assertEquals("no picture", photoService.getPhotoForThumbnail("some id"));

    }
}
