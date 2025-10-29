package dev.ctrlneo.roa.foundation.data.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stores the current level of a gun instance.
 * Each gun can be upgraded to increase its stats.
 * The level configuration (max level, stat bonuses per level) is stored in GunItem, not here.
 */
public record GunLevelComponent(
        int currentLevel
) {
    public static final GunLevelComponent DEFAULT = new GunLevelComponent(1);

    public static final Codec<GunLevelComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("currentLevel").forGetter(GunLevelComponent::currentLevel)
            ).apply(instance, GunLevelComponent::new)
    );

    public static final StreamCodec<ByteBuf, GunLevelComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GunLevelComponent::currentLevel,
                    GunLevelComponent::new
            );

    /**
     * Upgrade the gun to the next level.
     * @param maxLevel The maximum level this gun can reach
     * @return A new GunLevelComponent with the level increased (or same if at max)
     */
    public GunLevelComponent upgrade(int maxLevel) {
        if (currentLevel >= maxLevel) {
            return this; // Already at max level
        }
        return new GunLevelComponent(currentLevel + 1);
    }

    /**
     * Set the gun to a specific level.
     * @param level The level to set (clamped to 1-maxLevel range)
     * @param maxLevel The maximum level this gun can reach
     * @return A new GunLevelComponent with the specified level
     */
    public GunLevelComponent withLevel(int level, int maxLevel) {
        int clampedLevel = Math.max(1, Math.min(level, maxLevel));
        return new GunLevelComponent(clampedLevel);
    }

    /**
     * Check if this gun can be upgraded further.
     * @param maxLevel The maximum level this gun can reach
     * @return true if the gun can be upgraded
     */
    public boolean canUpgrade(int maxLevel) {
        return currentLevel < maxLevel;
    }
}

