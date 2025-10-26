package dev.ctrlneo.roa.foundation.mixin;

import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class WeaponTPRenderingMixin {
    @Inject(method = "getArmPose", at = @At(value = "TAIL"), cancellable = true)
    private static void tryItemPose(
            AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir
    ) {
        var itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() instanceof GunItem)
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
    }
}
