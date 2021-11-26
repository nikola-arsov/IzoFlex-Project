package bg.softuni.model.view;

import java.util.List;

public class CollectionItem implements Comparable<CollectionItem> {
    private String id;
    private String name;
    private boolean forSale;
    private List<String> locations;


    public CollectionItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    @Override
    public int compareTo(CollectionItem o) {
        return Boolean.compare(this.isForSale(), o.isForSale());
    }
}
