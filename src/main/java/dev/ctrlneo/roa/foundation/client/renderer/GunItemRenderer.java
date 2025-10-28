package dev.ctrlneo.roa.foundation.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ctrlneo.roa.foundation.animations.animators.GunItemAnimator;
import dev.ctrlneo.roa.foundation.utils.Reference;
import mod.azure.azurelib.common.render.item.AzItemRenderer;
import mod.azure.azurelib.common.render.item.AzItemRendererConfig;
import mod.azure.azurelib.common.render.item.AzItemRendererPipeline;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GunItemRenderer extends AzItemRenderer {
    private final ResourceLocation GEO, TEX;

    public GunItemRenderer(String name) {
        super(AzItemRendererConfig.builder(
                Reference.of("geo/item/weapons/%s.geo.json", name), Reference.of("textures/item/weapons/%s.png", name))
                .setAnimatorProvider(() -> new GunItemAnimator(name))
                .useNewOffset(true)
//                .setShouldAnimateInContext(ItemDisplayContext::firstPerson)
                .build()
        );
        GEO = Reference.of("geo/item/weapons/%s.geo.json", name);
        TEX = Reference.of("textures/item/weapons/%s.png", name);
    }
}
