package dev.ctrlneo.roa.foundation.utils;

import dev.ctrlneo.roa.Roa;
import net.minecraft.resources.ResourceLocation;

public class Reference {
    public static ResourceLocation of(String path, String... params) {
        return ResourceLocation.fromNamespaceAndPath(Roa.MODID,
                String.format(path, (Object[]) params));
    }
}
