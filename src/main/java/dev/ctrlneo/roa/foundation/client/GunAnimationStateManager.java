package dev.ctrlneo.roa.foundation.client;

import com.mojang.logging.LogUtils;
import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.RoaPackets;
import dev.ctrlneo.roa.foundation.data.components.GunStateComponent;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.packets.UpdateGunAnimationPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
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
    
    // Track when PLAY_ONCE animations are playing so we can return to loop animations
    private static long playOnceAnimationEndTick = 0;
    private static boolean needsLoopAnimationReturn = false;

    /**
     * Update the animation state based on player and gun state.
     * This should be called every client tick while holding a gun.
     */
    public static void updateAnimationState(LocalPlayer player, ItemStack gunStack, GunItem gunItem) {
        if (gunStack.isEmpty() || !(gunStack.getItem() instanceof GunItem)) {
            reset();
            return;
        }

        long currentTick = player.level().getGameTime();

        // Check if we need to return to loop animation after PLAY_ONCE finished
        if (needsLoopAnimationReturn && currentTick >= playOnceAnimationEndTick) {
            if (DEBUG) {
                LOGGER.info("[GunAnimator] PLAY_ONCE animation finished, returning to loop animation");
            }
            needsLoopAnimationReturn = false;
            // Force re-dispatch current loop animation
            AnimationState targetState = determineTargetState(player, gunStack);
            dispatchAnimation(player, gunStack, gunItem, targetState);
            currentState = targetState;
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
     * Dispatch the appropriate animation command for the target state.
     * Sends a packet to the server which will dispatch the animation command,
     * ensuring proper AzureLib synchronization to all clients.
     */
    private static void dispatchAnimation(LocalPlayer player, ItemStack gunStack, GunItem gunItem,
            AnimationState targetState) {
        switch (targetState) {
            case IDLE:
                if (DEBUG)
                    LOGGER.info("[GunAnimator] → Dispatching IDLE animation (via server)");
                RoaPackets.sendToServer(new UpdateGunAnimationPacket(
                        InteractionHand.MAIN_HAND,
                        UpdateGunAnimationPacket.AnimationState.IDLE));
                break;
            case AIM:
                if (DEBUG)
                    LOGGER.info("[GunAnimator] → Dispatching AIM animation (via server)");
                RoaPackets.sendToServer(new UpdateGunAnimationPacket(
                        InteractionHand.MAIN_HAND,
                        UpdateGunAnimationPacket.AnimationState.AIM));
                break;
            case SPRINT:
                if (DEBUG)
                    LOGGER.info("[GunAnimator] → Dispatching SPRINT animation (via server)");
                RoaPackets.sendToServer(new UpdateGunAnimationPacket(
                        InteractionHand.MAIN_HAND,
                        UpdateGunAnimationPacket.AnimationState.SPRINT));
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
            RoaPackets.sendToServer(new UpdateGunAnimationPacket(
                    InteractionHand.MAIN_HAND,
                    UpdateGunAnimationPacket.AnimationState.IDLE));
            currentState = AnimationState.IDLE;
        }
    }

    /**
     * Notify that a PLAY_ONCE animation has started.
     * After it finishes, the system will return to the appropriate loop animation.
     * 
     * @param durationTicks Duration of the PLAY_ONCE animation in ticks
     */
    public static void notifyPlayOnceAnimation(LocalPlayer player, int durationTicks) {
        long currentTick = player.level().getGameTime();
        playOnceAnimationEndTick = currentTick + durationTicks;
        needsLoopAnimationReturn = true;
        
        if (DEBUG) {
            LOGGER.info("[GunAnimator] PLAY_ONCE animation started, will return to loop after {} ticks", durationTicks);
        }
    }

    /**
     * Reset the animation state (called when no longer holding a gun)
     */
    public static void reset() {
        currentState = AnimationState.IDLE;
        needsLoopAnimationReturn = false;
        playOnceAnimationEndTick = 0;
    }

    /**
     * Get the current animation state (for debugging or external checks)
     */
    public static AnimationState getCurrentState() {
        return currentState;
    }
}
