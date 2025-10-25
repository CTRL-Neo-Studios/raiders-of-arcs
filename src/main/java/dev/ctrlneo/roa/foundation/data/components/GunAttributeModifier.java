package dev.ctrlneo.roa.foundation.data.components;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public record GunAttributeModifier(
        GunAttribute attribute,
        ModifierOperation operation,
        float value
) {
    public enum ModifierOperation implements StringRepresentable {
        ADD("add"),
        MULTIPLY_BASE("multiply_base"),
        MULTIPLY_TOTAL("multiply_total");

        public static final Codec<ModifierOperation> CODEC =
                StringRepresentable.fromEnum(ModifierOperation::values);

        public static final StreamCodec<ByteBuf, ModifierOperation> STREAM_CODEC =
                ByteBufCodecs.idMapper(i -> ModifierOperation.values()[i], ModifierOperation::ordinal);

        private final String name;

        ModifierOperation(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    public enum GunAttribute implements StringRepresentable {
        DAMAGE("damage"),
        ACCURACY("accuracy"),
        RECOIL_VERTICAL("recoil_vertical"),
        RECOIL_HORIZONTAL("recoil_horizontal"),
        FIRE_RATE("fire_rate"),
        RANGE("range"),
        ARMOR_PENETRATION("armor_penetration"),
        MAGAZINE_CAPACITY("magazine_capacity"),
        ADS_SPEED("ads_speed"),
        UNHOLSTER_SPEED("unholster_speed"),
        RELOAD_SPEED("reload_speed");

        public static final Codec<GunAttribute> CODEC =
                StringRepresentable.fromEnum(GunAttribute::values);

        public static final StreamCodec<ByteBuf, GunAttribute> STREAM_CODEC =
                ByteBufCodecs.idMapper(i -> GunAttribute.values()[i], GunAttribute::ordinal);

        private final String id;

        GunAttribute(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public @NotNull String getSerializedName() {
            return id;
        }
    }

    public float apply(float baseValue) {
        return switch (operation) {
            case ADD -> baseValue + value;
            case MULTIPLY_BASE -> baseValue * value;
            case MULTIPLY_TOTAL -> baseValue * value; // Applied separately
        };
    }

    // Codec for the whole record
    public static final Codec<GunAttributeModifier> CODEC =
            com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
                    GunAttribute.CODEC.fieldOf("attribute").forGetter(GunAttributeModifier::attribute),
                    ModifierOperation.CODEC.fieldOf("operation").forGetter(GunAttributeModifier::operation),
                    Codec.FLOAT.fieldOf("value").forGetter(GunAttributeModifier::value)
            ).apply(instance, GunAttributeModifier::new));

    // StreamCodec for network
    public static final StreamCodec<ByteBuf, GunAttributeModifier> STREAM_CODEC =
            StreamCodec.composite(
                    GunAttribute.STREAM_CODEC,
                    GunAttributeModifier::attribute,
                    ModifierOperation.STREAM_CODEC,
                    GunAttributeModifier::operation,
                    ByteBufCodecs.FLOAT,
                    GunAttributeModifier::value,
                    GunAttributeModifier::new
            );
}