package dev.ctrlneo.roa.foundation.animations.controller.core;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Functional interface for transition conditions.
 */
@FunctionalInterface
public interface TransitionCondition {
    boolean evaluate(LocalPlayer player, ItemStack itemStack);
}
