package dev.ctrlneo.roa.foundation.data.components;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.structures.AttachmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record GunAttachmentsComponent(
        ItemStack muzzle,
        ItemStack underbarrel,
        ItemStack magazine,
        ItemStack stock
) {
    public static final GunAttachmentsComponent EMPTY = new GunAttachmentsComponent(
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            ItemStack.EMPTY
    );

    /**
     * Get attachment in specific slot
     */
    public ItemStack getAttachment(AttachmentSlot slot) {
        return switch (slot) {
            case MUZZLE -> muzzle;
            case UNDERBARREL -> underbarrel;
            case MAGAZINE -> magazine;
            case STOCK -> stock;
        };
    }

    /**
     * Set attachment in specific slot
     */
    public GunAttachmentsComponent withAttachment(AttachmentSlot slot, ItemStack stack) {
        return switch (slot) {
            case MUZZLE -> new GunAttachmentsComponent(stack, underbarrel, magazine, stock);
            case UNDERBARREL -> new GunAttachmentsComponent(muzzle, stack, magazine, stock);
            case MAGAZINE -> new GunAttachmentsComponent(muzzle, underbarrel, stack, stock);
            case STOCK -> new GunAttachmentsComponent(muzzle, underbarrel, magazine, stack);
        };
    }

    /**
     * Remove attachment from specific slot
     */
    public GunAttachmentsComponent withoutAttachment(AttachmentSlot slot) {
        return withAttachment(slot, ItemStack.EMPTY);
    }

    /**
     * Get all attachments as a map
     */
    public Map<AttachmentSlot, ItemStack> getAllAttachments() {
        Map<AttachmentSlot, ItemStack> map = new HashMap<>();
        for (AttachmentSlot slot : AttachmentSlot.values()) {
            ItemStack stack = getAttachment(slot);
            if (!stack.isEmpty()) {
                map.put(slot, stack);
            }
        }
        return map;
    }

    /**
     * Combine all attachment modifiers into a single list.
     * Used by GunUtils to apply attachment effects using the unified modifier system.
     */
    public List<GunAttributeModifier> getCombinedModifiers() {
        List<GunAttributeModifier> combined = new ArrayList<>();

        for (AttachmentSlot slot : AttachmentSlot.values()) {
            ItemStack attachment = getAttachment(slot);
            if (!attachment.isEmpty()) {
                AttachmentModifiersComponent mods = attachment.get(RoaDataComponents.ATTACHMENT_MODIFIERS.get());
                if (mods != null) {
                    combined.addAll(mods.modifiers());
                }
            }
        }

        return combined;
    }

    /**
     * Check if any attachments are present
     */
    public boolean hasAnyAttachments() {
        return !muzzle.isEmpty() || !underbarrel.isEmpty() ||
                !magazine.isEmpty() || !stock.isEmpty();
    }

    /**
     * Count how many attachment slots are filled
     */
    public int getAttachmentCount() {
        int count = 0;
        if (!muzzle.isEmpty()) count++;
        if (!underbarrel.isEmpty()) count++;
        if (!magazine.isEmpty()) count++;
        if (!stock.isEmpty()) count++;
        return count;
    }
}