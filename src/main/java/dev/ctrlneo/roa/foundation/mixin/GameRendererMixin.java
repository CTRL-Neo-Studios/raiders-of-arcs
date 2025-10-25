package dev.ctrlneo.roa.foundation.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ctrlneo.roa.RoaConfig;
import dev.ctrlneo.roa.foundation.client.AdsStateManager;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@OnlyIn(Dist.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    /**
     * Smoothly modify FOV for ADS using partial ticks for silky smooth transitions
     */
    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private double modifyFovForADS(double originalFov, Camera camera, float partialTicks, boolean useFovSetting) {
        if (!(camera.getEntity() instanceof Player player)) {
            return originalFov;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            return originalFov;
        }

        // Get INTERPOLATED ADS progress using partial ticks - this is the key!
        float adsProgress = AdsStateManager.getAdsProgress(partialTicks);

        if (adsProgress <= 0.0f) {
            return originalFov;
        }

        // Calculate target FOV
        double adsFovMultiplier = RoaConfig.CLIENT.adsFovMultiplier.get();
        double targetFov = originalFov * adsFovMultiplier;

        // Apply smoothstep for even smoother feel
        float smoothProgress = roa$smoothstep(adsProgress);

        return Mth.lerp(smoothProgress, originalFov, targetFov);
    }

    /**
     * Smoothstep for ease-in-ease-out
     */
    @Unique
    private float roa$smoothstep(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    /**
     * Alternative: Use smootherstep for even more smoothness
     * Uncomment if you want an even smoother transition
     */
    @Unique
    private float roa$smootherstep(float t) {
        // Ken Perlin's smootherstep: 6t⁵ - 15t⁴ + 10t³
        return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
    }

    @WrapWithCondition(
            method = "renderItemInHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"
            )
    )
    private boolean disableVanillaBobForGuns(GameRenderer instance, PoseStack poseStack, float tickDelta) {
        if (minecraft.player == null) {
            return true;
        }

        ItemStack mainHandStack = minecraft.player.getMainHandItem();
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            return true;
        }

        // Use interpolated progress
        float adsProgress = AdsStateManager.getAdsProgress(tickDelta);
        if (adsProgress > 0.0f) {
            return false;
        }

        if (!minecraft.player.isSprinting()) {
            roa$gunBobView(poseStack, tickDelta);
        }

        return false;
    }

    @WrapWithCondition(
            method = "renderItemInHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"
            )
    )
    private boolean disableHurtBobForGuns(GameRenderer instance, PoseStack poseStack, float tickDelta) {
        if (minecraft.player == null) {
            return true;
        }

        ItemStack mainHandStack = minecraft.player.getMainHandItem();
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            return true;
        }

        return AdsStateManager.getAdsProgress(tickDelta) <= 0.0f;
    }

    @Unique
    private void roa$gunBobView(PoseStack poseStack, float tickDelta) {
        if (!(minecraft.getCameraEntity() instanceof Player player)) {
            return;
        }

        float walkDistance = player.walkDist - player.walkDistO;
        float walkSpeed = -(player.walkDist + walkDistance * tickDelta);
        float bob = Mth.lerp(tickDelta, player.oBob, player.bob);

        float bobIntensity = 0.25f;

        poseStack.translate(
                Mth.sin(walkSpeed * (float) Math.PI) * bob * 0.5f * bobIntensity,
                -Math.abs(Mth.cos(walkSpeed * (float) Math.PI) * bob) * bobIntensity,
                0.0f
        );

        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walkSpeed * (float) Math.PI) * bob * 3.0f * bobIntensity));
        poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(walkSpeed * (float) Math.PI - 0.2f) * bob) * 5.0f * bobIntensity));
    }
}