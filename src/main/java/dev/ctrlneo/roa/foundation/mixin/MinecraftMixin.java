package dev.ctrlneo.roa.foundation.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ctrlneo.roa.RoaConfig;
import dev.ctrlneo.roa.foundation.RoaPackets;
import dev.ctrlneo.roa.foundation.client.AdsStateManager;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.packets.AimDownSightsPacket;
import dev.ctrlneo.roa.foundation.network.packets.FireGunPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public LocalPlayer player;

    /**
     * Handle left-click (Fire) when holding a gun
     * This handles SINGLE fire and BURST fire modes
     * Automatic fire is handled in ClientTickHandler
     */
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onLeftClick(CallbackInfoReturnable<Boolean> cir) {
        if (player != null && player.getMainHandItem().getItem() instanceof GunItem) {
            // Send single fire packet
            // The server will check fire mode and handle accordingly
            RoaPackets.sendToServer(new FireGunPacket(InteractionHand.MAIN_HAND, true));

            // Cancel vanilla attack
            cir.setReturnValue(false);
        }
    }

    /**
     * Handle right-click (ADS) when holding a gun
     */
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void onRightClick(CallbackInfo ci) {
        if (player != null && player.getMainHandItem().getItem() instanceof GunItem) {
            boolean toggleAds = RoaConfig.CLIENT.toggleAds.get();
            boolean newAiming;

            if (toggleAds) {
                // Toggle mode - flip the state
                newAiming = !AdsStateManager.isPlayerAiming();
            } else {
                // Hold mode - always set to true on press
                // Release is handled in ClientTickHandler
                newAiming = true;
            }

            AdsStateManager.setAiming(newAiming);
            RoaPackets.sendToServer(new AimDownSightsPacket(InteractionHand.MAIN_HAND, newAiming));

            ci.cancel();
        }
    }

    /**
     * Prevent block breaking when holding a gun
     */
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void preventBlockBreaking(boolean leftClick, CallbackInfo ci) {
        if (player != null && player.getMainHandItem().getItem() instanceof GunItem) {
            ci.cancel();
        }
    }

    /**
     * Prevent hand swing animation when using a gun
     */
    @ModifyExpressionValue(
            method = "startUseItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult;shouldSwing()Z")
    )
    private boolean dontSwingGun(boolean original) {
        return original && !(player != null && player.getMainHandItem().getItem() instanceof GunItem);
    }
}