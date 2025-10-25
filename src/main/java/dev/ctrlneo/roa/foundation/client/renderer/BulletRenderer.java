package dev.ctrlneo.roa.foundation.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ctrlneo.roa.foundation.entity.BulletEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BulletRenderer extends EntityRenderer<BulletEntity> {

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BulletEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Bullets are tiny and fast - usually just particles are enough
        // You can add a small model here if you want visible bullets
    }

    @Override
    public ResourceLocation getTextureLocation(BulletEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/entity/arrow.png");
    }
}