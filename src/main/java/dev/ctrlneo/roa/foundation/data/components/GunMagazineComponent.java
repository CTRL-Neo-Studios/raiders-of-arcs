package dev.ctrlneo.roa.foundation.data.components;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

public record GunMagazineComponent(
        int currentAmmo,
        int baseCapacity,      // Base capacity without attachments
        Holder<Item> ammoType
) {
    /**
     * Calculate effective capacity including magazine attachments
     */
    public int getEffectiveCapacity(GunAttachmentsComponent attachments) {
        int capacity = baseCapacity;

        if (attachments != null && !attachments.magazine().isEmpty()) {
            AttachmentModifiersComponent mods = attachments.magazine()
                    .get(RoaDataComponents.ATTACHMENT_MODIFIERS.get());
            if (mods != null) {
                capacity += mods.magazineCapacityBonus();
            }
        }

        return capacity;
    }

    public GunMagazineComponent withAmmo(int newAmount, GunAttachmentsComponent attachments) {
        int maxCap = getEffectiveCapacity(attachments);
        return new GunMagazineComponent(
                Math.min(newAmount, maxCap),
                baseCapacity,
                ammoType
        );
    }

    public GunMagazineComponent consume(int amount) {
        return new GunMagazineComponent(
                Math.max(0, currentAmmo - amount),
                baseCapacity,
                ammoType
        );
    }

    public GunMagazineComponent reload(int amountToAdd, GunAttachmentsComponent attachments) {
        return withAmmo(currentAmmo + amountToAdd, attachments);
    }

    public boolean canFire() {
        return currentAmmo > 0;
    }

    public boolean needsReload(GunAttachmentsComponent attachments) {
        return currentAmmo < getEffectiveCapacity(attachments);
    }

    public boolean isEmpty() {
        return currentAmmo == 0;
    }

    public boolean isFull(GunAttachmentsComponent attachments) {
        return currentAmmo >= getEffectiveCapacity(attachments);
    }
}