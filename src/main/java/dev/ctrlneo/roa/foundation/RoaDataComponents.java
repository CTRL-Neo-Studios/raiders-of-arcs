package dev.ctrlneo.roa.foundation;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.data.codecs.RoaDataCodecs;
import dev.ctrlneo.roa.foundation.data.components.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RoaDataComponents {

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GunFireModesComponent>> GUN_FIRE_MODES =
            Roa.DATA_COMPONENTS.register("gun_fire_modes", () ->
                    DataComponentType.<GunFireModesComponent>builder()
                            .persistent(RoaDataCodecs.GUN_FIRE_MODES_COMPONENT_CODEC)
                            .networkSynchronized(RoaDataCodecs.STREAM_GUN_FIRE_MODES_COMPONENT_CODEC)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GunMagazineComponent>> GUN_MAGAZINE =
            Roa.DATA_COMPONENTS.register("gun_magazine", () ->
                    DataComponentType.<GunMagazineComponent>builder()
                            .persistent(RoaDataCodecs.GUN_MAGAZINE_COMPONENT_CODEC)
                            .networkSynchronized(RoaDataCodecs.STREAM_GUN_MAGAZINE_COMPONENT_CODEC)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GunStatsComponent>> GUN_STATS =
            Roa.DATA_COMPONENTS.register("gun_stats", () ->
                    DataComponentType.<GunStatsComponent>builder()
                            .persistent(RoaDataCodecs.GUN_STATS_COMPONENT_CODEC)
                            .networkSynchronized(RoaDataCodecs.STREAM_GUN_STATS_COMPONENT_CODEC)
                            .cacheEncoding()
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GunStateComponent>> GUN_STATE =
            Roa.DATA_COMPONENTS.register("gun_state", () ->
                    DataComponentType.<GunStateComponent>builder()
                            .persistent(RoaDataCodecs.GUN_STATE_COMPONENT_CODEC)
                            .networkSynchronized(RoaDataCodecs.STREAM_GUN_STATE_COMPONENT_CODEC)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GunAttachmentsComponent>> GUN_ATTACHMENTS =
            Roa.DATA_COMPONENTS.register("gun_attachments", () ->
                    DataComponentType.<GunAttachmentsComponent>builder()
                            .persistent(RoaDataCodecs.GUN_ATTACHMENTS_COMPONENT_CODEC)
                            .networkSynchronized(RoaDataCodecs.STREAM_GUN_ATTACHMENTS_COMPONENT_CODEC)
                            .build());

    // Attachment components
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AttachmentModifiersComponent>> ATTACHMENT_MODIFIERS =
            Roa.DATA_COMPONENTS.register("attachment_modifiers", () ->
                    DataComponentType.<AttachmentModifiersComponent>builder()
                            .persistent(RoaDataCodecs.ATTACHMENT_MODIFIERS_COMPONENT_CODEC)
                            .networkSynchronized(RoaDataCodecs.STREAM_ATTACHMENT_MODIFIERS_COMPONENT_CODEC)
                            .cacheEncoding()
                            .build());


    public static void register() {

    }
}
