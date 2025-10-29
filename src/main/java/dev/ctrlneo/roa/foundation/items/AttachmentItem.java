package dev.ctrlneo.roa.foundation.items;

import dev.ctrlneo.roa.foundation.data.structures.AttachmentSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AttachmentItem extends Item {
    private final AttachmentSlot slot;

    public AttachmentItem(Properties properties, AttachmentSlot slot) {
        super(properties.stacksTo(1));
        this.slot = slot;
    }

    public AttachmentSlot getSlot() {
        return slot;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // Just show the attachment slot
        tooltipComponents.add(Component.translatable(
                "tooltip.roa.attachment_slot",
                slot.getDisplayName()
        ).withStyle(ChatFormatting.GRAY));

        // Note: Detailed modifier stats are shown when the attachment is equipped on the gun
    }
}