package dev.ctrlneo.roa.foundation.client;

import com.mojang.logging.LogUtils;
import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.GunStateComponent;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

/**
 * Manages which animation state should be playing for a gun based on player
 * state.
 * This tracks the last played animation to avoid spamming animation commands.
 */
public class GunAnimationStateManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG = true; // Set to false to disable debug logs

    public enum AnimationState {
        IDLE,
        AIM,
        SPRINT,
        RELOADING,
        FIRING // Note: Firing is handled separately as a one-shot animation
    }

    private static AnimationState currentState = AnimationState.IDLE;

    /**
     * Update the animation state based on player and gun state.
     * This should be called every client tick while holding a gun.
     */
    public static void updateAnimationState(LocalPlayer player, ItemStack gunStack, GunItem gunItem) {
        if (gunStack.isEmpty() || !(gunStack.getItem() instanceof GunItem)) {
            reset();
            return;
        }

        // Determine what state we should be in
        AnimationState targetState = determineTargetState(player, gunStack);

        // Only dispatch animation if state changed
        if (targetState != currentState) {
            if (DEBUG) {
                LOGGER.info("[GunAnimator] State changed: {} -> {} | Item: {}",
                        currentState, targetState, gunStack.getItem().toString());
            }
            dispatchAnimation(player, gunStack, gunItem, targetState);
            currentState = targetState;
        }
    }

    /**
     * Determine what animation state the gun should be in based on player state
     */
    private static AnimationState determineTargetState(LocalPlayer player, ItemStack gunStack) {
        // Priority order: Reloading > Sprinting > Aiming > Idle

        // Check if reloading (highest priority - can't do anything else)
        GunStateComponent state = gunStack.get(RoaDataComponents.GUN_STATE.get());
        if (state != null && state.isReloading()) {
            return AnimationState.RELOADING;
        }

        // Check if sprinting (can't aim while sprinting)
        if (player.isSprinting()) {
            return AnimationState.SPRINT;
        }

        // Check if aiming
        if (AdsStateManager.isPlayerAiming()) {
            return AnimationState.AIM;
        }

        // Default to idle
        return AnimationState.IDLE;
    }

    /**
     * Dispatch the appropriate animation command for the target state
     */
    private static void dispatchAnimation(LocalPlayer player, ItemStack gunStack, GunItem gunItem,
            AnimationState targetState) {
        switch (targetState) {
            case IDLE:
                if (DEBUG)
                    LOGGER.info("[GunAnimator] → Dispatching IDLE animation");
                gunItem.dispatcher.idle(player, gunStack);
                break;
            case AIM:
                if (DEBUG)
                    LOGGER.info("[GunAnimator] → Dispatching AIM animation");
                gunItem.dispatcher.aim(player, gunStack);
                break;
            case SPRINT:
                if (DEBUG)
                    LOGGER.info("[GunAnimator] → Dispatching SPRINT animation");
                gunItem.dispatcher.sprint(player, gunStack);
                break;
            case RELOADING:
                if (DEBUG)
                    LOGGER.info("[GunAnimator] → State: RELOADING (animation already playing)");
                // Note: Reload animation is triggered by GunItem.reload()
                // We don't need to trigger it here, but we track the state
                // to prevent other animations from overriding it
                break;
            case FIRING:
                if (DEBUG)
                    LOGGER.info("[GunAnimator] → State: FIRING (one-shot)");
                // Fire animations are handled separately in GunItem.tryFire()
                // as they are one-shot animations that play on top of the loop
                break;
        }
    }

    /**
     * Force a state transition to idle.
     * Useful when reload completes or is cancelled.
     */
    public static void transitionToIdle(LocalPlayer player, ItemStack gunStack, GunItem gunItem) {
        if (currentState != AnimationState.IDLE) {
            gunItem.dispatcher.idle(player, gunStack);
            currentState = AnimationState.IDLE;
        }
    }

    /**
     * Reset the animation state (called when no longer holding a gun)
     */
    public static void reset() {
        currentState = AnimationState.IDLE;
    }

    /**
     * Get the current animation state (for debugging or external checks)
     */
    public static AnimationState getCurrentState() {
        return currentState;
    }
}
