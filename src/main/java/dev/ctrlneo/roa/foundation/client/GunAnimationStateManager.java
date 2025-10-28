package dev.ctrlneo.roa.foundation.client;

import com.mojang.logging.LogUtils;
import dev.ctrlneo.roa.foundation.RoaPackets;
import dev.ctrlneo.roa.foundation.animations.controller.AnimatorController;
import dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController;
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
        AnimatorController.AnimationCommand command = controller.update(player, gunStack);

        // If state changed, send animation command to server
        if (command != null) {
            if (DEBUG) {
                LOGGER.info("[GunAnimator] State changed to: {} (duration: {} ticks)", 
                        command.getAnimationName(), 
                        command.getDurationTicks());
            }
            sendAnimationCommand(command);
        }
    }

    /**
     * Notify that the player fired the gun.
     * This triggers the appropriate fire animation.
     */
    public static void notifyFire(LocalPlayer player, ItemStack gunStack, GunItem gunItem, boolean isAiming) {
        AnimatorController controller = controllerInstances.computeIfAbsent(
                gunItem,
                item -> item.animatorController
        );

        if (controller instanceof GunAnimatorController gunController) {
            AnimatorController.AnimationState fireState = gunController.getFireState(isAiming);
            AnimatorController.AnimationCommand command = controller.forceState(fireState, player);
            
            if (DEBUG) {
                LOGGER.info("[GunAnimator] FIRE animation: {} (duration: {} ticks)",
                        command.getAnimationName(),
                        command.getDurationTicks());
            }
            
            sendAnimationCommand(command);
        }
    }

    /**
     * Notify that the player started reloading.
     * This triggers the reload animation.
     */
    public static void notifyReload(LocalPlayer player, ItemStack gunStack, GunItem gunItem) {
        AnimatorController controller = controllerInstances.computeIfAbsent(
                gunItem,
                item -> item.animatorController
        );

        if (controller instanceof GunAnimatorController gunController) {
            AnimatorController.AnimationState reloadState = gunController.getReloadState();
            AnimatorController.AnimationCommand command = controller.forceState(reloadState, player);
            
            if (DEBUG) {
                LOGGER.info("[GunAnimator] RELOAD animation: {} (duration: {} ticks)",
                        command.getAnimationName(),
                        command.getDurationTicks());
            }
            
            sendAnimationCommand(command);
        }
    }

    /**
     * Send animation command to server for synchronization.
     * Now sends the actual animation name and play behavior from the AnimatorController!
     */
    private static void sendAnimationCommand(AnimatorController.AnimationCommand command) {
        // Get play behavior ordinal for network transmission
        int playBehaviorOrdinal = getPlayBehaviorOrdinal(command.getPlayBehavior());
        
        RoaPackets.sendToServer(new UpdateGunAnimationPacket(
                InteractionHand.MAIN_HAND,
                command.getAnimationName(),
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
