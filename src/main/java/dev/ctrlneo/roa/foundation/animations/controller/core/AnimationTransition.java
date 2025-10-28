package dev.ctrlneo.roa.foundation.animations.controller.core;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Represents a transition between two states with a condition.
 */
public record AnimationTransition(AnimationState from, AnimationState to,
                                  TransitionCondition condition) {

    public boolean evaluateCondition(LocalPlayer player, ItemStack itemStack) {
        return condition.evaluate(player, itemStack);
    }
}
