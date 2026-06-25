package model;

public abstract class Item implements Interactable, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String description;

    protected Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns the type discriminator for this item.
     */
    public abstract ItemType getItemType();
}
