package model;

public abstract class Item implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String description;

    protected Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract String interact(Player player);

    public abstract ItemType getItemType();
}
