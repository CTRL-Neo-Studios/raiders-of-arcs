package dev.ctrlneo.roa.foundation.client;

import com.mojang.logging.LogUtils;
import dev.ctrlneo.roa.foundation.RoaPackets;
import dev.ctrlneo.roa.foundation.animations.controller.AnimatorController;
import dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController;
import dev.ctrlneo.roa.foundation.animations.controller.core.AnimationCommand;
import dev.ctrlneo.roa.foundation.animations.controller.core.AnimationState;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.packets.UpdateGunAnimationPacket;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages gun animations using per-gun AnimatorController instances.
 * This uses a Unity Mechanim-style state machine approach for flexibility.
 */
public class GunAnimationStateManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG = true; // Set to false to disable debug logs

    // Store animator controller instances per item (one instance per gun type)
    private static final Map<GunItem, AnimatorController> controllerInstances = new HashMap<>();

    /**
     * Update the animation state based on player and gun state.
     * This should be called every client tick while holding a gun.
     */
    public static void updateAnimationState(LocalPlayer player, ItemStack gunStack, GunItem gunItem) {
        if (gunStack.isEmpty() || !(gunStack.getItem() instanceof GunItem)) {
            reset();
            return;
        }

        // Get or create animator controller instance for this gun
        AnimatorController controller = controllerInstances.computeIfAbsent(
                gunItem,
                item -> item.animatorController
        );

        // Update the controller's state machine
        AnimationCommand command = controller.update(player, gunStack);

        // If state changed, send animation command to server
        if (command != null) {
            if (DEBUG) {
                LOGGER.info("[GunAnimator] State changed to: {} (duration: {} ticks)", 
                        command.animationName(),
                        command.durationTicks());
            }
            sendAnimationCommand(command);
        }
    }

    /**
     * Notify that the player fired the gun.
     * NO LONGER SENDS PACKET - animation is dispatched server-side for efficiency!
     */
    public static void notifyFire(LocalPlayer player, ItemStack gunStack, GunItem gunItem, boolean isAiming) {
        // Fire animations are now dispatched directly from GunItem.tryFire() on the server
        // This eliminates redundant packet sending (server already knows gun fired via FireGunPacket)
        // We just update the local controller state for tracking
        AnimatorController controller = controllerInstances.computeIfAbsent(
                gunItem,
                item -> item.animatorController
        );

        if (controller instanceof GunAnimatorController gunController) {
            AnimationState fireState = gunController.getFireState(isAiming);
            controller.forceState(fireState, player);
            
            if (DEBUG) {
                LOGGER.info("[GunAnimator] FIRE animation: {} (server will dispatch)",
                        fireState.getAnimationName());
            }
        }
    }

    /**
     * Notify that the player started reloading.
     * NO LONGER SENDS PACKET - animation is dispatched server-side for efficiency!
     */
    public static void notifyReload(LocalPlayer player, ItemStack gunStack, GunItem gunItem) {
        // Reload animations are now dispatched directly from GunItem.reload() on the server
        // This eliminates redundant packet sending (server already knows reload started via ReloadGunPacket)
        // We just update the local controller state for tracking
        AnimatorController controller = controllerInstances.computeIfAbsent(
                gunItem,
                item -> item.animatorController
        );

        if (controller instanceof GunAnimatorController gunController) {
            AnimationState reloadState = gunController.getReloadState();
            controller.forceState(reloadState, player);
            
            if (DEBUG) {
                LOGGER.info("[GunAnimator] RELOAD animation: {} (server will dispatch)",
                        reloadState.getAnimationName());
            }
        }
    }

    /**
     * Send animation command to server for synchronization.
     * Now sends the actual animation name and play behavior from the AnimatorController!
     */
    private static void sendAnimationCommand(AnimationCommand command) {
        // Get play behavior ordinal for network transmission
        int playBehaviorOrdinal = getPlayBehaviorOrdinal(command.playBehavior());
        
        RoaPackets.sendToServer(new UpdateGunAnimationPacket(
                InteractionHand.MAIN_HAND,
                command.animationName(),
                playBehaviorOrdinal
        ));
    }
    
    /**
     * Get ordinal for AzPlayBehavior for network transmission.
     * This matches the mapping in UpdateGunAnimationPacket.
     */
    private static int getPlayBehaviorOrdinal(AzPlayBehavior behavior) {
        if (behavior == AzPlayBehaviors.LOOP) return 0;
        if (behavior == AzPlayBehaviors.PLAY_ONCE) return 1;
        if (behavior == AzPlayBehaviors.HOLD_ON_LAST_FRAME) return 2;
        return 0; // Default to LOOP
    }

    /**
     * Reset the animation state (called when no longer holding a gun)
     */
    public static void reset() {
        // Reset all controller instances
        for (AnimatorController controller : controllerInstances.values()) {
            controller.reset();
        }
    }
}
