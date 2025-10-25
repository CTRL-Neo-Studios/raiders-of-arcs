package dev.ctrlneo.roa.foundation.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ctrlneo.roa.RoaConfig;
import dev.ctrlneo.roa.foundation.client.AdsStateManager;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@OnlyIn(Dist.CLIENT)
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow @Final private Minecraft minecraft;

    @ModifyExpressionValue(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0)
    )
    private Object modifySensitivity(Object originalSensitivity) {
        if (!AdsStateManager.isPlayerAiming()) {
            return originalSensitivity;
        }

        LocalPlayer player = minecraft.player;
        if (player == null) {
            return originalSensitivity;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            return originalSensitivity;
        }

        // Modify sensitivity when aiming
        if (originalSensitivity instanceof Double sensitivity) {
            return sensitivity * RoaConfig.CLIENT.adsSensitivityMultiplier.get();
        }

        return originalSensitivity;
    }
}