package dev.ctrlneo.roa.foundation.items;

import dev.ctrlneo.roa.foundation.data.components.AttachmentModifiersComponent;
import dev.ctrlneo.roa.foundation.data.components.RoaDataComponents;
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

        tooltipComponents.add(Component.translatable(
                "tooltip.roa.attachment_slot",
                slot.getDisplayName()
        ).withStyle(ChatFormatting.GRAY));

        AttachmentModifiersComponent mods = stack.get(RoaDataComponents.ATTACHMENT_MODIFIERS.get());
        if (mods != null) {
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("tooltip.roa.modifiers")
                    .withStyle(ChatFormatting.GOLD));

            // Additive bonuses
            if (mods.damageBonus() != 0) {
                addModifierTooltip(tooltipComponents, "damage", mods.damageBonus(), true);
            }
            if (mods.rangeBonus() != 0) {
                addModifierTooltip(tooltipComponents, "range", mods.rangeBonus(), true);
            }
            if (mods.magazineCapacityBonus() != 0) {
                tooltipComponents.add(Component.translatable(
                        "tooltip.roa.modifier.magazine_capacity",
                        (mods.magazineCapacityBonus() > 0 ? "+" : "") + mods.magazineCapacityBonus()
                ).withStyle(mods.magazineCapacityBonus() > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
            if (mods.armorPenetrationBonus() != 0) {
                addModifierTooltip(tooltipComponents, "armor_penetration", mods.armorPenetrationBonus(), true);
            }

            // Multiplicative modifiers
            if (mods.accuracyMultiplier() != 1.0f) {
                addPercentModifier(tooltipComponents, "accuracy", mods.accuracyMultiplier());
            }
            if (mods.recoilMultiplier() != 1.0f) {
                addPercentModifier(tooltipComponents, "recoil", mods.recoilMultiplier());
            }
            if (mods.adsSpeedMultiplier() != 1.0f) {
                addPercentModifier(tooltipComponents, "ads_speed", mods.adsSpeedMultiplier());
            }
            if (mods.reloadSpeedMultiplier() != 1.0f) {
                addPercentModifier(tooltipComponents, "reload_speed", mods.reloadSpeedMultiplier());
            }
        }
    }

    private void addModifierTooltip(List<Component> tooltips, String key, float value, boolean higherIsBetter) {
        ChatFormatting color = (higherIsBetter ? value > 0 : value < 0) ?
                ChatFormatting.GREEN : ChatFormatting.RED;

        tooltips.add(Component.translatable(
                "tooltip.roa.modifier." + key,
                String.format("%+.1f", value)
        ).withStyle(color));
    }

    private void addPercentModifier(List<Component> tooltips, String key, float multiplier) {
        float percent = (multiplier - 1.0f) * 100;
        boolean isPositive = percent > 0;

        // For some stats, lower is better (recoil, ADS/reload speed)
        boolean lowerIsBetter = key.equals("recoil") || key.contains("speed");
        ChatFormatting color = (lowerIsBetter ? !isPositive : isPositive) ?
                ChatFormatting.GREEN : ChatFormatting.RED;

        tooltips.add(Component.translatable(
                "tooltip.roa.modifier." + key,
                String.format("%+.0f%%", percent)
        ).withStyle(color));
    }
}