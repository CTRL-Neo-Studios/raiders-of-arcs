package dev.ctrlneo.roa.foundation.client.renderer;

import dev.ctrlneo.roa.foundation.animations.animators.GunItemAnimator;
import dev.ctrlneo.roa.foundation.utils.Reference;
import mod.azure.azurelib.common.render.item.AzItemRenderer;
import mod.azure.azurelib.common.render.item.AzItemRendererConfig;
import net.minecraft.resources.ResourceLocation;

public class GunItemRenderer extends AzItemRenderer {
    private final ResourceLocation GEO, TEX;

    public GunItemRenderer(String name) {
        super(AzItemRendererConfig.builder(
                Reference.of("geo/item/weapons/%s.geo.json", name), Reference.of("textures/item/weapons/%s.png", name))
                .setAnimatorProvider(() -> new GunItemAnimator(name))
                .useNewOffset(true)
                .build()
        );
        GEO = Reference.of("geo/item/weapons/%s.geo.json", name);
        TEX = Reference.of("textures/item/weapons/%s.png", name);
    }
}
