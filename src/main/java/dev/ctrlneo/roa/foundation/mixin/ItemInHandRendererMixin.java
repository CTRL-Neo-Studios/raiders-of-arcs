package dev.ctrlneo.roa.foundation.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ctrlneo.roa.foundation.client.AdsStateManager;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress);

    /**
     * Apply custom transformations for guns to prevent bobbing but keep equip animation
     */
//    @Inject(
//            method = "renderArmWithItem",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
//                    shift = At.Shift.AFTER
//            )
//    )
//    private void modifyGunTransform(
//            AbstractClientPlayer player,
//            float partialTicks,
//            float pitch,
//            InteractionHand hand,
//            float swingProgress,
//            ItemStack stack,
//            float equipProgress,
//            PoseStack poseStack,
//            net.minecraft.client.renderer.MultiBufferSource bufferSource,
//            int combinedLight,
//            CallbackInfo ci
//    ) {
//        if (!(stack.getItem() instanceof GunItem)) {
//            return;
//        }
//
//        // Apply custom gun positioning
//        // This ensures the gun is in the correct position without vanilla sway
//        roa$applyGunTransform(poseStack, hand, equipProgress);
//    }
//
//    @Unique
//    private void roa$applyGunTransform(PoseStack poseStack, InteractionHand hand, float equipProgress) {
//        // Apply equip animation (rising up motion)
//        // This is the key part - it ensures the item rises to the viewport
//        float equipOffset = 1.0F - equipProgress;
//        poseStack.translate(0.0F, equipOffset * -0.6F, 0.0F);
//
//        // You can add custom positioning here for your gun models
//        // For example, moving guns slightly forward:
//        // poseStack.translate(0.0F, 0.0F, -0.1F);
//    }
}