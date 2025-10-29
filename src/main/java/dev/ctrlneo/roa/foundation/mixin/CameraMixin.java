package dev.ctrlneo.roa.foundation.mixin;

import net.minecraft.client.Camera;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Camera mixin for potential future camera-related modifications.
 * 
 * Note: Recoil is now handled by directly modifying player rotation in RecoilManager,
 * not camera rotation. This ensures bullets go where the crosshair is pointing.
 */
@OnlyIn(Dist.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {
    // Recoil application moved to RecoilManager for bullet accuracy
}

