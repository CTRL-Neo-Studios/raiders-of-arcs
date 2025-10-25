package dev.ctrlneo.roa.foundation.data.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class RoaDataCodecs {
    public static final Codec<GunFireModesComponent> GUN_FIRE_MODES_COMPONENT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    GunFireMode.CODEC
                            .fieldOf("current_mode")
                            .forGetter(GunFireModesComponent::currentMode),
                    GunFireMode.CODEC.listOf()
                            .fieldOf("available_modes")
                            .forGetter(GunFireModesComponent::availableModes)
            ).apply(instance, GunFireModesComponent::new));

    public static final StreamCodec<ByteBuf, GunFireModesComponent> STREAM_GUN_FIRE_MODES_COMPONENT_CODEC =
            StreamCodec.composite(
                    GunFireMode.STREAM_CODEC,
                    GunFireModesComponent::currentMode,
                    GunFireMode.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    GunFireModesComponent::availableModes,
                    GunFireModesComponent::new
            );

    public static final Codec<GunMagazineComponent> GUN_MAGAZINE_COMPONENT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("current_ammo").forGetter(GunMagazineComponent::currentAmmo),
                    Codec.INT.fieldOf("base_capacity").forGetter(GunMagazineComponent::baseCapacity),
                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("ammo_type").forGetter(GunMagazineComponent::ammoType)
            ).apply(instance, GunMagazineComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GunMagazineComponent> STREAM_GUN_MAGAZINE_COMPONENT_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    GunMagazineComponent::currentAmmo,
                    ByteBufCodecs.VAR_INT,
                    GunMagazineComponent::baseCapacity,
                    ByteBufCodecs.holderRegistry(Registries.ITEM),
                    GunMagazineComponent::ammoType,
                    GunMagazineComponent::new
            );

    public static final Codec<GunStatsComponent> GUN_STATS_COMPONENT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("damage").forGetter(GunStatsComponent::damage),
                    Codec.FLOAT.fieldOf("accuracy").forGetter(GunStatsComponent::accuracy),
                    Codec.FLOAT.fieldOf("recoil_vertical").forGetter(GunStatsComponent::recoilVertical),
                    Codec.FLOAT.fieldOf("recoil_horizontal").forGetter(GunStatsComponent::recoilHorizontal),
                    Codec.INT.fieldOf("fire_rate").forGetter(GunStatsComponent::fireRate),
                    Codec.FLOAT.fieldOf("range").forGetter(GunStatsComponent::range),
                    Codec.FLOAT.fieldOf("armor_penetration").forGetter(GunStatsComponent::armorPenetration),
                    Codec.FLOAT.fieldOf("ads_speed").forGetter(GunStatsComponent::adsSpeed),
                    Codec.FLOAT.fieldOf("unholster_speed").forGetter(GunStatsComponent::unholsterSpeed),
                    Codec.FLOAT.fieldOf("reload_speed").forGetter(GunStatsComponent::reloadSpeed)
            ).apply(instance, GunStatsComponent::new));

    public static final StreamCodec<ByteBuf, GunStatsComponent> STREAM_GUN_STATS_COMPONENT_CODEC =
            new StreamCodec<>() {
                @Override
                public GunStatsComponent decode(ByteBuf buffer) {
                    return new GunStatsComponent(
                            buffer.readFloat(),  // damage
                            buffer.readFloat(),  // accuracy
                            buffer.readFloat(),  // recoilVertical
                            buffer.readFloat(),  // recoilHorizontal
                            buffer.readInt(),    // fireRate
                            buffer.readFloat(),  // range
                            buffer.readFloat(),  // armorPenetration
                            buffer.readFloat(),  // adsSpeed
                            buffer.readFloat(),  // unholsterSpeed
                            buffer.readFloat()   // reloadSpeed
                    );
                }

                @Override
                public void encode(ByteBuf buffer, GunStatsComponent value) {
                    buffer.writeFloat(value.damage());
                    buffer.writeFloat(value.accuracy());
                    buffer.writeFloat(value.recoilVertical());
                    buffer.writeFloat(value.recoilHorizontal());
                    buffer.writeInt(value.fireRate());
                    buffer.writeFloat(value.range());
                    buffer.writeFloat(value.armorPenetration());
                    buffer.writeFloat(value.adsSpeed());
                    buffer.writeFloat(value.unholsterSpeed());
                    buffer.writeFloat(value.reloadSpeed());
                }
            };




    public static final Codec<GunStateComponent> GUN_STATE_COMPONENT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.fieldOf("is_reloading").forGetter(GunStateComponent::isReloading),
                    Codec.LONG.fieldOf("last_fire_time").forGetter(GunStateComponent::lastFireTime),
                    Codec.INT.fieldOf("burst_shots_fired").forGetter(GunStateComponent::burstShotsFired)
            ).apply(instance, GunStateComponent::new));

    public static final StreamCodec<ByteBuf, GunStateComponent> STREAM_GUN_STATE_COMPONENT_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, GunStateComponent::isReloading,
                    ByteBufCodecs.VAR_LONG, GunStateComponent::lastFireTime,
                    ByteBufCodecs.VAR_INT, GunStateComponent::burstShotsFired,
                    GunStateComponent::new
            );

    public static final Codec<AttachmentModifiersComponent> ATTACHMENT_MODIFIERS_COMPONENT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("damage_bonus").forGetter(AttachmentModifiersComponent::damageBonus),
                    Codec.FLOAT.fieldOf("range_bonus").forGetter(AttachmentModifiersComponent::rangeBonus),
                    Codec.INT.fieldOf("magazine_capacity_bonus").forGetter(AttachmentModifiersComponent::magazineCapacityBonus),
                    Codec.FLOAT.fieldOf("armor_penetration_bonus").forGetter(AttachmentModifiersComponent::armorPenetrationBonus),
                    Codec.FLOAT.fieldOf("accuracy_multiplier").forGetter(AttachmentModifiersComponent::accuracyMultiplier),
                    Codec.FLOAT.fieldOf("recoil_multiplier").forGetter(AttachmentModifiersComponent::recoilMultiplier),
                    Codec.FLOAT.fieldOf("ads_speed_multiplier").forGetter(AttachmentModifiersComponent::adsSpeedMultiplier),
                    Codec.FLOAT.fieldOf("reload_speed_multiplier").forGetter(AttachmentModifiersComponent::reloadSpeedMultiplier)
            ).apply(instance, AttachmentModifiersComponent::new));

    // Note: Composite only supports 6 fields, so we need custom codec for the last 2
    public static final StreamCodec<ByteBuf, AttachmentModifiersComponent> STREAM_ATTACHMENT_MODIFIERS_COMPONENT_CODEC_FULL =
            new StreamCodec<>() {
                @Override
                public AttachmentModifiersComponent decode(ByteBuf buffer) {
                    return new AttachmentModifiersComponent(
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readInt(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat(),
                            buffer.readFloat()
                    );
                }

                @Override
                public void encode(ByteBuf buffer, AttachmentModifiersComponent value) {
                    buffer.writeFloat(value.damageBonus());
                    buffer.writeFloat(value.rangeBonus());
                    buffer.writeInt(value.magazineCapacityBonus());
                    buffer.writeFloat(value.armorPenetrationBonus());
                    buffer.writeFloat(value.accuracyMultiplier());
                    buffer.writeFloat(value.recoilMultiplier());
                    buffer.writeFloat(value.adsSpeedMultiplier());
                    buffer.writeFloat(value.reloadSpeedMultiplier());
                }
            };

    public static final Codec<GunAttachmentsComponent> GUN_ATTACHMENTS_COMPONENT_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.CODEC.fieldOf("muzzle").forGetter(GunAttachmentsComponent::muzzle),
                    ItemStack.CODEC.fieldOf("underbarrel").forGetter(GunAttachmentsComponent::underbarrel),
                    ItemStack.CODEC.fieldOf("magazine").forGetter(GunAttachmentsComponent::magazine),
                    ItemStack.CODEC.fieldOf("stock").forGetter(GunAttachmentsComponent::stock)
            ).apply(instance, GunAttachmentsComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GunAttachmentsComponent> STREAM_GUN_ATTACHMENTS_COMPONENT_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC, GunAttachmentsComponent::muzzle,
                    ItemStack.STREAM_CODEC, GunAttachmentsComponent::underbarrel,
                    ItemStack.STREAM_CODEC, GunAttachmentsComponent::magazine,
                    ItemStack.STREAM_CODEC, GunAttachmentsComponent::stock,
                    GunAttachmentsComponent::new
            );
}