package dev.ctrlneo.roa.foundation.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ctrlneo.roa.foundation.client.RecoilManager;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mixin to apply smooth recoil to camera rotation during rendering.
 * Uses partial ticks for silky smooth interpolation like FOV changes.
 */
@OnlyIn(Dist.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    public abstract Entity getEntity();

    /**
     * Modify pitch (vertical rotation) with interpolated recoil.
     * This is called during camera setup with partial ticks!
     */
    @ModifyExpressionValue(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"
            )
    )
    private float applyRecoilPitch(float originalPitch, net.minecraft.world.level.BlockGetter area,
                                   Entity entity, boolean thirdPerson, boolean inverseView, float partialTick) {
        if (!(entity instanceof Player player)) {
            return originalPitch;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            return originalPitch;
        }

        // Apply interpolated recoil with partial ticks - this is the key to smoothness!
        float recoilPitch = RecoilManager.getInterpolatedPitch(partialTick);
        return originalPitch + recoilPitch;
    }

    /**
     * Modify yaw (horizontal rotation) with interpolated recoil.
     */
    @ModifyExpressionValue(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"
            )
    )
    private float applyRecoilYaw(float originalYaw, net.minecraft.world.level.BlockGetter area,
                                 Entity entity, boolean thirdPerson, boolean inverseView, float partialTick) {
        if (!(entity instanceof Player player)) {
            return originalYaw;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            return originalYaw;
        }

        // Apply interpolated recoil with partial ticks
        float recoilYaw = RecoilManager.getInterpolatedYaw(partialTick);
        return originalYaw + recoilYaw;
    }
}

