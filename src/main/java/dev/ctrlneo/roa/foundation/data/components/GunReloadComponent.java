package dev.ctrlneo.roa.foundation.data.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ctrlneo.roa.foundation.data.structures.ReloadType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines reload behavior for a gun.
 * All reload timing and configuration is centralized here.
 */
public record GunReloadComponent(
        ReloadType reloadType,
        float reloadDuration,  // Duration in seconds: for ONE_SHOT = full reload, for SEQUENTIAL = per-sequence
        int sequenceSize       // How many rounds per sequence (1, 2, 3, etc.) - only used for SEQUENTIAL
) {
    public static final Codec<GunReloadComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(ReloadType::valueOf, ReloadType::name).fieldOf("reloadType").forGetter(GunReloadComponent::reloadType),
                    Codec.FLOAT.fieldOf("reloadDuration").forGetter(GunReloadComponent::reloadDuration),
                    Codec.INT.fieldOf("sequenceSize").forGetter(GunReloadComponent::sequenceSize)
            ).apply(instance, GunReloadComponent::new)
    );

    public static final StreamCodec<ByteBuf, GunReloadComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(i -> ReloadType.values()[i], ReloadType::ordinal),
            GunReloadComponent::reloadType,
            ByteBufCodecs.FLOAT,
            GunReloadComponent::reloadDuration,
            ByteBufCodecs.VAR_INT,
            GunReloadComponent::sequenceSize,
            GunReloadComponent::new
    );

    /**
     * Default: One-shot reload, 2 seconds, 1 round (not used for ONE_SHOT)
     */
    public static final GunReloadComponent DEFAULT = new GunReloadComponent(
            ReloadType.ONE_SHOT,
            2.0f,
            1
    );

    /**
     * Get reload duration in ticks.
     * For ONE_SHOT: total reload time
     * For SEQUENTIAL: time per sequence
     */
    public int getReloadDurationTicks() {
        return Math.round(reloadDuration * 20.0f);
    }

    /**
     * Calculate how many rounds to reload this sequence.
     * @param currentAmmo Current ammo in magazine
     * @param maxCapacity Maximum magazine capacity
     * @return Number of rounds to add this sequence
     */
    public int getRoundsThisSequence(int currentAmmo, int maxCapacity) {
        int remaining = maxCapacity - currentAmmo;
        return Math.min(sequenceSize, remaining);
    }

    /**
     * Check if we need more reload sequences.
     */
    public boolean needsMoreReload(int currentAmmo, int maxCapacity) {
        return currentAmmo < maxCapacity;
    }
}

