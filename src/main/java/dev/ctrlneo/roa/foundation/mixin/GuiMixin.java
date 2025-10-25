package dev.ctrlneo.roa.foundation.mixin;

import dev.ctrlneo.roa.RoaConfig;
import dev.ctrlneo.roa.foundation.client.AdsStateManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void hideCrosshairWhileADS(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (AdsStateManager.isPlayerAiming() && !RoaConfig.CLIENT.showCrosshairWhileAds.get()) {
            ci.cancel();
        }
    }
}