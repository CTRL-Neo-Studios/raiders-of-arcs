package dev.ctrlneo.roa.foundation.utils;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.structures.AttachmentSlot;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class GunUtils {

    /**
     * Creates a gun ItemStack in its default state:
     * - Base stats (no attachment modifiers)
     * - Empty magazine
     * - No attachments
     * - Default gun state
     */
    public static ItemStack getDefaultGun(GunItem gunItem) {
        ItemStack stack = new ItemStack(gunItem);

        // Get the base components from the item's default instance
        ItemStack defaultStack = gunItem.getDefaultInstance();

        // Copy base stats
        GunStatsComponent baseStats = defaultStack.get(RoaDataComponents.GUN_STATS.get());
        if (baseStats != null) {
            stack.set(RoaDataComponents.GUN_STATS.get(), baseStats);
        }

        // Set empty magazine with base capacity
        GunMagazineComponent baseMagazine = defaultStack.get(RoaDataComponents.GUN_MAGAZINE.get());
        if (baseMagazine != null) {
            stack.set(RoaDataComponents.GUN_MAGAZINE.get(), new GunMagazineComponent(
                    0,  // Empty
                    baseMagazine.baseCapacity(),
                    baseMagazine.ammoType()
            ));
        }

        // Copy fire modes
        GunFireModesComponent fireModes = defaultStack.get(RoaDataComponents.GUN_FIRE_MODES.get());
        if (fireModes != null) {
            stack.set(RoaDataComponents.GUN_FIRE_MODES.get(), fireModes);
        }

        // Reset state
        stack.set(RoaDataComponents.GUN_STATE.get(), GunStateComponent.DEFAULT);

        // No attachments
        stack.set(RoaDataComponents.GUN_ATTACHMENTS.get(), GunAttachmentsComponent.EMPTY);

        return stack;
    }

    /**
     * Gets the effective stats of a gun including attachment modifiers
     */
    public static GunStatsComponent getEffectiveStats(ItemStack gunStack) {
        GunStatsComponent baseStats = gunStack.get(RoaDataComponents.GUN_STATS.get());
        if (baseStats == null) return GunStatsComponent.DEFAULT;

        GunAttachmentsComponent attachments = gunStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        if (attachments == null || !attachments.hasAnyAttachments()) {
            return baseStats;
        }

        AttachmentModifiersComponent combinedMods = attachments.getCombinedModifiers();
        return baseStats.withAttachmentModifiers(combinedMods);
    }

    /**
     * Strips all attachments from a gun and returns them
     */
    public static Map<AttachmentSlot, ItemStack> removeAllAttachments(ItemStack gunStack) {
        GunAttachmentsComponent attachments = gunStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        if (attachments == null) return Map.of();

        Map<AttachmentSlot, ItemStack> removed = attachments.getAllAttachments();
        gunStack.set(RoaDataComponents.GUN_ATTACHMENTS.get(), GunAttachmentsComponent.EMPTY);

        return removed;
    }
}