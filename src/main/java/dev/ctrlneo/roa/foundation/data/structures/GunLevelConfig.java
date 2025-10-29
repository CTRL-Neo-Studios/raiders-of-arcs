package dev.ctrlneo.roa.foundation.data.structures;

import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for a gun's leveling system.
 * Defines the maximum level and what stat modifiers apply at each level.
 * 
 * Modifiers are CUMULATIVE - a level 3 gun gets modifiers from levels 2 AND 3.
 * Uses the unified GunAttributeModifier system for consistency with attachments.
 */
public record GunLevelConfig(
        int maxLevel,
        Map<Integer, List<GunAttributeModifier>> modifiersPerLevel
) {
    /**
     * Default config: max level 1, no upgrades possible.
     */
    public static final GunLevelConfig DEFAULT = new GunLevelConfig(1, Map.of());

    /**
     * Get the cumulative modifiers for a specific level.
     * This combines all modifiers from level 2 through the specified level.
     * (Level 1 has no modifiers - it's the base)
     * 
     * @param level The level to get cumulative modifiers for
     * @return The list of all level modifiers up to the specified level
     */
    public List<GunAttributeModifier> getCumulativeModifiers(int level) {
        if (level <= 1) {
            return LevelModifiers.NONE;
        }

        List<GunAttributeModifier> cumulative = new ArrayList<>();
        
        // Collect all modifiers from level 2 through current level
        for (int i = 2; i <= level; i++) {
            List<GunAttributeModifier> levelMods = modifiersPerLevel.getOrDefault(i, List.of());
            cumulative.addAll(levelMods);
        }

        return cumulative;
    }

    /**
     * Builder for creating gun level configurations with a fluent API.
     */
    public static class Builder {
        private int maxLevel = 1;
        private final Map<Integer, List<GunAttributeModifier>> modifiers = new HashMap<>();

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = Math.max(1, maxLevel);
            return this;
        }

        public Builder level(int level, List<GunAttributeModifier> modifiers) {
            if (level > 1) { // Level 1 doesn't need modifiers (it's the base)
                this.modifiers.put(level, List.copyOf(modifiers));
            }
            return this;
        }

        public GunLevelConfig build() {
            return new GunLevelConfig(maxLevel, new HashMap<>(modifiers));
        }
    }

    /**
     * Create a new builder for gun level config.
     */
    public static Builder builder() {
        return new Builder();
    }
}

