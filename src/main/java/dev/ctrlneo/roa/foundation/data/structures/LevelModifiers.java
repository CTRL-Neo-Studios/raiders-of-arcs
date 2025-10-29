package dev.ctrlneo.roa.foundation.data.structures;

import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier;
import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier.GunAttribute;
import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier.ModifierOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for building level modifiers using the unified GunAttributeModifier system.
 * Provides a fluent API for defining stat bonuses/penalties per level.
 * 
 * Example:
 * <pre>
 * List<GunAttributeModifier> levelMods = LevelModifiers.builder()
 *     .add(GunAttribute.DAMAGE, 2.0f)                    // +2 damage
 *     .add(GunAttribute.MAGAZINE_CAPACITY, 5.0f)         // +5 rounds
 *     .multiply(GunAttribute.RECOIL_VERTICAL, 0.9f)      // -10% recoil
 *     .build();
 * </pre>
 */
public class LevelModifiers {
    /**
     * Empty modifier list (no bonuses/penalties).
     */
    public static final List<GunAttributeModifier> NONE = List.of();

    /**
     * Builder for creating level modifiers with a fluent API.
     */
    public static class Builder {
        private final List<GunAttributeModifier> modifiers = new ArrayList<>();

        /**
         * Add an additive modifier (ADD operation).
         * @param attribute The attribute to modify
         * @param value The value to add (can be negative for penalties)
         */
        public Builder add(GunAttribute attribute, float value) {
            modifiers.add(new GunAttributeModifier(attribute, ModifierOperation.ADD, value));
            return this;
        }

        /**
         * Add a multiplicative base modifier (MULTIPLY_BASE operation).
         * @param attribute The attribute to modify
         * @param multiplier The multiplier (e.g., 1.1 = +10%, 0.9 = -10%)
         */
        public Builder multiplyBase(GunAttribute attribute, float multiplier) {
            modifiers.add(new GunAttributeModifier(attribute, ModifierOperation.MULTIPLY_BASE, multiplier));
            return this;
        }

        /**
         * Add a multiplicative total modifier (MULTIPLY_TOTAL operation).
         * @param attribute The attribute to modify
         * @param multiplier The multiplier (e.g., 1.1 = +10%, 0.9 = -10%)
         */
        public Builder multiplyTotal(GunAttribute attribute, float multiplier) {
            modifiers.add(new GunAttributeModifier(attribute, ModifierOperation.MULTIPLY_TOTAL, multiplier));
            return this;
        }

        /**
         * Add a custom modifier directly.
         */
        public Builder modifier(GunAttributeModifier modifier) {
            modifiers.add(modifier);
            return this;
        }

        public List<GunAttributeModifier> build() {
            return List.copyOf(modifiers);
        }
    }

    /**
     * Create a new builder for level modifiers.
     */
    public static Builder builder() {
        return new Builder();
    }
}

