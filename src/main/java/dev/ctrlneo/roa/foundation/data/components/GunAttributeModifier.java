package dev.ctrlneo.roa.foundation.data.structures;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public record GunAttributeModifier(
        GunAttribute attribute,
        ModifierOperation operation,
        float value
) {
    public enum ModifierOperation {
        ADD,           // +10 damage
        MULTIPLY_BASE, // Base value * 1.5
        MULTIPLY_TOTAL // Final value * 1.5 (applied last)
    }

    public enum GunAttribute {
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

        private final String id;

        GunAttribute(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static final Codec<GunAttribute> CODEC =
                StringRepresentable.fromEnum(GunAttribute::values);
    }

    public float apply(float baseValue) {
        return switch (operation) {
            case ADD -> baseValue + value;
            case MULTIPLY_BASE -> baseValue * value;
            case MULTIPLY_TOTAL -> baseValue * value; // Applied separately
        };
    }
}