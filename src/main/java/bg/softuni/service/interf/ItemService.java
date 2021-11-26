package bg.softuni.service.interf;

import bg.softuni.model.entity.Item;
import bg.softuni.model.entity.Offer;
import bg.softuni.model.view.InfoItem;
import bg.softuni.model.binding.ItemFormModel;
import bg.softuni.model.service.ItemServiceModel;

public interface ItemService {
    ItemServiceModel saveItem(String username, ItemFormModel item);

    ItemServiceModel editItem(String username, ItemFormModel item);

    InfoItem getItemForInfoPage(String id, String username);

    ItemServiceModel removeItem(String id, String ownerUsername);

    Item validateOwnerAndItemId(String id, String ownerUsername, String message);

    boolean toggleForSale(Item item);

    ItemServiceModel buyItem(Offer offer, String username);

    ItemFormModel getItemViewById(String principal,String id);
}