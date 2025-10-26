package dev.ctrlneo.roa.foundation;

import mod.azure.azurelib.common.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.common.render.item.AzItemRenderer;
import mod.azure.azurelib.common.render.item.AzItemRendererRegistry;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RoaItemRenderers {
    public static class Entry {
        public Supplier<AzItemRenderer> renderer;
        public DeferredItem<?> mainItem;

        public Entry(Supplier<AzItemRenderer> renderer, DeferredItem<?> mainItem) {
            this.renderer = renderer;
            this.mainItem = mainItem;
        }
    }

    public static List<Entry> RENDERERS = new ArrayList<>();

    public static void register() {
        RENDERERS.forEach(entry -> {
            AzItemRendererRegistry.register(entry.renderer, entry.mainItem.get());
            AzIdentityRegistry.register(entry.mainItem.get());
        });
    }
}
