package model;

/**
 * Discriminator for Item subtypes, enabling type-safe switches
 * without instanceof checks.
 */
public enum ItemType {
    CONSUMABLE,
    SHIELD,
    KEY,
    DIAMOND
}
