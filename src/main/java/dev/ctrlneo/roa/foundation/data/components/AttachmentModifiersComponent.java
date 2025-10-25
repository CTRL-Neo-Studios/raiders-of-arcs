package dev.ctrlneo.roa.foundation.data.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Describes stat modifications provided by an attachment.
 * Additive bonuses are added to base values.
 * Multipliers: 1.0 = no change, 1.1 = +10%, 0.9 = -10%
 */
public record AttachmentModifiersComponent(
        // Additive bonuses
        float damageBonus,
        float rangeBonus,
        int magazineCapacityBonus,
        float armorPenetrationBonus,

        // Multiplicative modifiers
        float accuracyMultiplier,
        float recoilMultiplier,
        float adsSpeedMultiplier,
        float reloadSpeedMultiplier
) {
    public static final AttachmentModifiersComponent EMPTY = new AttachmentModifiersComponent(
            0.0f, 0.0f, 0, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    );

    // Codec for JSON/NBT serialization
    public static final Codec<AttachmentModifiersComponent> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.optionalFieldOf("damage_bonus", 0.0f).forGetter(AttachmentModifiersComponent::damageBonus),
                    Codec.FLOAT.optionalFieldOf("range_bonus", 0.0f).forGetter(AttachmentModifiersComponent::rangeBonus),
                    Codec.INT.optionalFieldOf("magazine_capacity_bonus", 0).forGetter(AttachmentModifiersComponent::magazineCapacityBonus),
                    Codec.FLOAT.optionalFieldOf("armor_penetration_bonus", 0.0f).forGetter(AttachmentModifiersComponent::armorPenetrationBonus),
                    Codec.FLOAT.optionalFieldOf("accuracy_multiplier", 1.0f).forGetter(AttachmentModifiersComponent::accuracyMultiplier),
                    Codec.FLOAT.optionalFieldOf("recoil_multiplier", 1.0f).forGetter(AttachmentModifiersComponent::recoilMultiplier),
                    Codec.FLOAT.optionalFieldOf("ads_speed_multiplier", 1.0f).forGetter(AttachmentModifiersComponent::adsSpeedMultiplier),
                    Codec.FLOAT.optionalFieldOf("reload_speed_multiplier", 1.0f).forGetter(AttachmentModifiersComponent::reloadSpeedMultiplier)
            ).apply(instance, AttachmentModifiersComponent::new));

    // StreamCodec for network synchronization
    public static final StreamCodec<ByteBuf, AttachmentModifiersComponent> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AttachmentModifiersComponent decode(ByteBuf buffer) {
                    return new AttachmentModifiersComponent(
                            buffer.readFloat(),  // damageBonus
                            buffer.readFloat(),  // rangeBonus
                            buffer.readInt(),    // magazineCapacityBonus
                            buffer.readFloat(),  // armorPenetrationBonus
                            buffer.readFloat(),  // accuracyMultiplier
                            buffer.readFloat(),  // recoilMultiplier
                            buffer.readFloat(),  // adsSpeedMultiplier
                            buffer.readFloat()   // reloadSpeedMultiplier
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

    /**
     * Combines multiple modifiers (for stacking multiple attachments)
     */
    public AttachmentModifiersComponent combine(AttachmentModifiersComponent other) {
        return new AttachmentModifiersComponent(
                this.damageBonus + other.damageBonus,
                this.rangeBonus + other.rangeBonus,
                this.magazineCapacityBonus + other.magazineCapacityBonus,
                this.armorPenetrationBonus + other.armorPenetrationBonus,
                this.accuracyMultiplier * other.accuracyMultiplier,
                this.recoilMultiplier * other.recoilMultiplier,
                this.adsSpeedMultiplier * other.adsSpeedMultiplier,
                this.reloadSpeedMultiplier * other.reloadSpeedMultiplier
        );
    }

    /**
     * Checks if this modifier actually modifies anything
     */
    public boolean isEmpty() {
        return damageBonus == 0.0f
                && rangeBonus == 0.0f
                && magazineCapacityBonus == 0
                && armorPenetrationBonus == 0.0f
                && accuracyMultiplier == 1.0f
                && recoilMultiplier == 1.0f
                && adsSpeedMultiplier == 1.0f
                && reloadSpeedMultiplier == 1.0f;
    }
}