package dev.ctrlneo.roa.foundation.animations.animators;

import dev.ctrlneo.roa.foundation.utils.Reference;
import mod.azure.azurelib.common.animation.controller.AzAnimationController;
import mod.azure.azurelib.common.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.common.animation.impl.AzItemAnimator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GunItemAnimator extends AzItemAnimator {
    private final ResourceLocation ANIMATIONS;

    public GunItemAnimator(String name) {
        super();
        ANIMATIONS = Reference.of("animations/item/weapons/%s.animation.json", name);
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<ItemStack> animationControllerContainer) {
        animationControllerContainer.add(
                AzAnimationController.builder(this, "base_controller")
                        .setTransitionLength(1)
                        .build());
    }

    @Override
    public @NotNull ResourceLocation getAnimationLocation(ItemStack animatable) {
        return ANIMATIONS;
    }
}
