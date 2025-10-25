package dev.ctrlneo.roa.foundation.data.structures;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum GunFireMode implements StringRepresentable {
    SINGLE_FIRE("single_fire", 0, "Single Fire", false, 1),
    BURST2_FIRE("2_round_burst_fire", 1, "2-Round Burst Fire", true, 2),
    BURST3_FIRE("3_round_burst_fire", 2, "3-Round Burst Fire", true, 3),
    AUTOMATIC_FIRE("automatic_fire", 3, "Automatic Fire", true, -1); // -1 for continuous

    public static final Codec<GunFireMode> CODEC = StringRepresentable.fromEnum(GunFireMode::values);
    public static final StreamCodec<ByteBuf, GunFireMode> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> GunFireMode.values()[i], GunFireMode::ordinal);

    private static final Map<String, GunFireMode> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(GunFireMode::getSerializedName, Function.identity()));
    private static final Map<Integer, GunFireMode> BY_VALUE = Arrays.stream(values())
            .collect(Collectors.toMap(GunFireMode::getValue, Function.identity()));

    private final String name;
    private final int value;
    private final String displayName;
    private final boolean continuous;
    private final int burstCount;

    GunFireMode(String name, int value, String displayName, boolean continuous, int burstCount) {
        this.name = name;
        this.value = value;
        this.displayName = displayName;
        this.continuous = continuous;
        this.burstCount = burstCount;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public int getBurstCount() {
        return burstCount;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static GunFireMode byName(String name) {
        return BY_NAME.getOrDefault(name, SINGLE_FIRE);
    }

    public static GunFireMode byValue(int value) {
        return BY_VALUE.getOrDefault(value, SINGLE_FIRE);
    }

    public GunFireMode next() {
        GunFireMode[] modes = values();
        return modes[(this.ordinal() + 1) % modes.length];
    }

    @Override
    public String toString() {
        return displayName;
    }
}