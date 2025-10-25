package dev.ctrlneo.roa.foundation.data.structures;

import dev.ctrlneo.roa.foundation.RoaItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public enum AmmoType {
    LIGHT(RoaItems.LIGHT_AMMO, 100),
    MEDIUM(RoaItems.MEDIUM_AMMO, 80),
    HEAVY(RoaItems.HEAVY_AMMO, 40),
    SHOTGUN(RoaItems.SHOTGUN_AMMO, 20),
    LAUNCHER(RoaItems.LAUNCHER_AMMO, 24),
    ENERGY(RoaItems.ENERGY_CLIP, 5);

    private final DeferredItem<Item> itemSupplier;
    private final int maxStackSize;

    AmmoType(DeferredItem<Item> itemSupplier, int maxStackSize) {
        this.itemSupplier = itemSupplier;
        this.maxStackSize = maxStackSize;
    }

    /**
     * Gets the Holder for use in GunMagazineComponent
     */
    public Holder<Item> getHolder() {
        return itemSupplier.getDelegate();
    }

    /**
     * Gets the actual Item instance
     */
    public Item getItem() {
        return itemSupplier.get();
    }

    /**
     * Gets the max stack size for this ammo type
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Gets the DeferredItem for this ammo type
     */
    public DeferredItem<Item> getDeferredItem() {
        return itemSupplier;
    }

    /**
     * Checks if the given item is this ammo type
     */
    public boolean isAmmo(Item item) {
        return itemSupplier.get() == item;
    }
}