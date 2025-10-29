package dev.ctrlneo.roa.foundation.data.components;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier.GunAttribute;
import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier.ModifierOperation;
import dev.ctrlneo.roa.foundation.utils.GunUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GunMagazineComponent(
        int currentAmmo,
        int baseCapacity,      // Base capacity without attachments
        Holder<Item> ammoType
) {
    /**
     * Calculate effective capacity including level bonuses and magazine attachments.
     * This is the preferred method when you have the ItemStack available.
     */
    public int getEffectiveCapacity(ItemStack gunStack, GunAttachmentsComponent attachments) {
        int capacity = baseCapacity;

        // Add level bonuses
        capacity += GunUtils.getLevelMagazineBonus(gunStack);

        // Add attachment bonuses
        if (attachments != null && !attachments.magazine().isEmpty()) {
            AttachmentModifiersComponent mods = attachments.magazine()
                    .get(RoaDataComponents.ATTACHMENT_MODIFIERS.get());
            if (mods != null) {
                // Extract MAGAZINE_CAPACITY modifiers from the new unified system
                List<GunAttributeModifier> modifiers = mods.modifiers();
                for (GunAttributeModifier mod : modifiers) {
                    if (mod.attribute() == GunAttribute.MAGAZINE_CAPACITY && mod.operation() == ModifierOperation.ADD) {
                        capacity += (int) mod.value();
                    }
                }
            }
        }

        return capacity;
    }

    /**
     * Calculate effective capacity including only magazine attachments (no level bonuses).
     * Use the overload with ItemStack parameter when possible.
     * @deprecated Use getEffectiveCapacity(ItemStack, GunAttachmentsComponent) instead
     */
    @Deprecated
    public int getEffectiveCapacity(GunAttachmentsComponent attachments) {
        int capacity = baseCapacity;

        if (attachments != null && !attachments.magazine().isEmpty()) {
            AttachmentModifiersComponent mods = attachments.magazine()
                    .get(RoaDataComponents.ATTACHMENT_MODIFIERS.get());
            if (mods != null) {
                // Extract MAGAZINE_CAPACITY modifiers from the new unified system
                List<GunAttributeModifier> modifiers = mods.modifiers();
                for (GunAttributeModifier mod : modifiers) {
                    if (mod.attribute() == GunAttribute.MAGAZINE_CAPACITY && mod.operation() == ModifierOperation.ADD) {
                        capacity += (int) mod.value();
                    }
                }
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