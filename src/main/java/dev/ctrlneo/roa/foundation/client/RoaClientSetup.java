package dev.ctrlneo.roa.foundation.client;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.RoaEntityTypes;
import dev.ctrlneo.roa.foundation.client.renderer.BulletRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Roa.MODID, value = Dist.CLIENT)
public class RoaClientSetup {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RoaEntityTypes.BULLET.get(), BulletRenderer::new);
    }
}