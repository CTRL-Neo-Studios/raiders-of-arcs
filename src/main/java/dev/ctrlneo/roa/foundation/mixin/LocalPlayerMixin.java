package dev.ctrlneo.roa.foundation.mixin;

import com.mojang.authlib.GameProfile;
import dev.ctrlneo.roa.foundation.client.AdsStateManager;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to prevent sprint jitter when players hold the sprint button while aiming.
 * Extends AbstractClientPlayer to access super.setSprinting() for proper state management.
 * 
 * Based on a proven pattern from Fabric gun mods (adapted for NeoForge).
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    // Dummy constructor required for mixin extension
    public LocalPlayerMixin(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }
    
    /**
     * Inject at the very start of aiStep() to suppress sprinting while aiming.
     * 
     * aiStep() is called every tick and handles movement input including sprinting.
     * By injecting at HEAD with highest priority, we override sprint state BEFORE
     * any vanilla logic that might try to enable it.
     * 
     * This prevents:
     * - Animation jitter between aim/sprint states
     * - Excessive packet sending
     * - State inconsistencies when holding sprint key while aiming
     */
    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void roa$blockSprintWhileAiming(CallbackInfo ci) {
        if (this.getMainHandItem().getItem() instanceof GunItem && AdsStateManager.isPlayerAiming()) {
            if (this.isSprinting()) {
                super.setSprinting(false);
                ci.cancel();
            }
        }
    }

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void roa$blockSprintWhileAimingServer(CallbackInfo ci) {
        if (this.getMainHandItem().getItem() instanceof GunItem && AdsStateManager.isPlayerAiming()) {
            if (this.isSprinting()) {
                super.setSprinting(false);
            }
        }
    }
}

