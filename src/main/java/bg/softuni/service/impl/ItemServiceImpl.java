package bg.softuni.service.impl;

import bg.softuni.model.binding.MoneyTransactionModel;
import bg.softuni.model.entity.Item;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.entity.Photo;
import bg.softuni.model.entity.User;
import bg.softuni.model.enumeration.Category;
import bg.softuni.model.view.InfoItem;
import bg.softuni.repository.ItemRepository;
import bg.softuni.service.interf.ItemService;
import bg.softuni.service.interf.PhotoService;
import bg.softuni.service.interf.UserService;
import bg.softuni.util.core.MultipartFileHandler;
import bg.softuni.util.core.ValidatorUtil;
import bg.softuni.util.handler.EnumValidator;
import bg.softuni.model.binding.ItemFormModel;
import bg.softuni.model.service.ItemServiceModel;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final MultipartFileHandler fileHandler;
    private final PhotoService photoService;
    private final ItemRepository repository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final ValidatorUtil validator;

    @Autowired
    public ItemServiceImpl(MultipartFileHandler fileHandler, PhotoService photoService, UserService userService, ItemRepository repository, ModelMapper modelMapper, ValidatorUtil validator) {
        this.fileHandler = fileHandler;
        this.photoService = photoService;
        this.userService = userService;
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.validator = validator;
    }

    @Override
    public ItemServiceModel saveItem(String username, ItemFormModel item) {
        User owner = this.validateEntityForAddItem(username, item);
        Item temp = this.modelMapper.map(item, Item.class);

        temp.setOwner(owner);
        temp.setCategory(Category.valueOf(item.getCategory()));
        temp.setPhotos(this.mapUrlsToPhotos(username, item, temp));

        return this.modelMapper.map(this.repository.saveAndFlush(temp), ItemServiceModel.class);
    }

    @Override
    public ItemServiceModel editItem(String username, ItemFormModel item) {
        Item original = this.getItemById(item.getId());
        String hasNewPhotos = this.validateEntityForEditItem(item);

        this.checkIfPrincipalIsOwner(username, original, "edit id");

        original.setName(item.getName());
        original.setCategory(Category.valueOf(item.getCategory()));
        original.setDescription(item.getDescription());

        if (!"unchanged".equals(hasNewPhotos)) {
            this.removePictures(original);
            original.setPhotos(this.mapUrlsToPhotos(username, item, original));
        }
        return this.modelMapper.map(this.repository.saveAndFlush(original), ItemServiceModel.class);
    }

    @Override
    public InfoItem getItemForInfoPage(String id, String ownerUsername) {
        Item item = this.validateOwnerAndItemId(id, ownerUsername, "view the Info page");
        InfoItem output = this.modelMapper.map(item, InfoItem.class);
        output.setPhoto(this.photoService.getPhotoForThumbnail(item.getId()));

        return output;
    }

    @Override
    public ItemServiceModel removeItem(String id, String ownerUsername) {
        Item toBeRemoved = this.validateOwnerAndItemId(id, ownerUsername, "remove it");
        this.throwIfItemIsForSale(toBeRemoved, "removed");

        if (!this.removePictures(toBeRemoved)) {
            throw new IllegalArgumentException("Removing of saved photos failed!");
        }
        this.repository.delete(toBeRemoved);

        return this.modelMapper.map(toBeRemoved, ItemServiceModel.class);
    }

    @Override
    public Item validateOwnerAndItemId(String id, String ownerUsername, String message) {
        Item item = this.getItemById(id);
        this.checkIfPrincipalIsOwner(ownerUsername, item, message);

        return item;
    }

    @Override
    public ItemFormModel getItemViewById(String principal, String id) {
        Item item = this.getItemById(id);
        this.checkIfPrincipalIsOwner(principal, item, "edit it");
        this.throwIfItemIsForSale(item, "edited");

        return this.modelMapper.map(item, ItemFormModel.class);
    }

    @Override
    public boolean toggleForSale(Item item) {
        item.setForSale(!item.isForSale());
        return this.repository.saveAndFlush(item).isForSale();
    }

    @Override
    public ItemServiceModel buyItem(Offer offer, String username) {
        if (offer.getSeller().getUsername().equals(username)){
            throw new IllegalArgumentException("You can't biy tour own item!");
        }

        User user = this.userService.getUserByUsername(username);
        this.userService.withdraw(username, new MoneyTransactionModel(offer.getPrice()));
        this.userService.deposit(offer.getSeller().getUsername(), new MoneyTransactionModel(offer.getPrice()));
        Item item = offer.getItem();

        this.userService.newNotification(item.getOwner()
                , String.format("Потребителя: %s купи Вашия продукт: %s за %s лева."
                        , user.getUsername(), item.getName(), offer.getPrice()));

        item.setForSale(false);
        item.setOwner(user);

        return this.modelMapper.map(this.repository.saveAndFlush(item), ItemServiceModel.class);
    }

    private boolean removePictures(Item item) {
        boolean flag = true;
        for (Photo photo : item.getPhotos()) {
            File file = new File(photo.getLocation().substring(1));
            boolean result = file.delete();
            this.photoService.deletePicture(photo.getId());

            if (!result) {
                flag = false;
            }
        }
        return flag;
    }

    private User validateEntityForAddItem(String username, ItemFormModel item) {
        this.validateModelAndEnum(item);

        item.getImages().forEach(i -> {
            if (i == null || i.isEmpty() || ("").equals(i.getOriginalFilename())) {
                throw new IllegalStateException("Received photo is empty, please enter a valid photo in .jpg / .png format.");
            }
        });//Check if there is an invalid img
        return this.userService.getUserByUsername(username);//check if user exists
    }

    private String validateEntityForEditItem(ItemFormModel item) {
        this.validateModelAndEnum(item);

        for (MultipartFile img : item.getImages()) {
            if (("").equals(img.getOriginalFilename())) {
                return "unchanged";
            }
        }//Check if there is images
        return "has_new";
    }

    private void validateModelAndEnum(ItemFormModel item) {
        if (!this.validator.isValid(item)) {
            throw new IllegalStateException("The item has invalid fields, check them and resubmit later.");
        }//Check binding model for errors

        EnumValidator.validateEnum(item.getCategory(), Category.class, "");//Check if enum value is valid
    }

    private Set<Photo> mapUrlsToPhotos(String username, ItemFormModel item, Item temp) {
        Set<String> urls = item.getImages().stream()
                .map(u -> this.fileHandler.saveFile(username, u)).collect(Collectors.toSet());

        return urls.stream().map(u -> new Photo(u, temp)).collect(Collectors.toSet());
    }

    private <T extends Item> void checkIfPrincipalIsOwner(String username, T model, String message) {
        if (!model.getOwner().getUsername().equals(username)) {
            throw new IllegalArgumentException("You are not the owner of that item, you can't " + message + "!");
        }
    }

    private Item getItemById(String id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item with that id doesn't exist!"));
    }

    private void throwIfItemIsForSale(Item item, String message) {
        if (item.isForSale()) {
            throw new IllegalArgumentException("Item can't be " + message + ", item tagged for sale!");
        }
    }
}