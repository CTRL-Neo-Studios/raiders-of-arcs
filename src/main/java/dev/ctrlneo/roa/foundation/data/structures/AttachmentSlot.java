package dev.ctrlneo.roa.foundation.data.structures;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AttachmentSlot implements StringRepresentable {
    MUZZLE("muzzle", "Muzzle"),
    UNDERBARREL("underbarrel", "Underbarrel"),
    MAGAZINE("magazine", "Magazine"),
    STOCK("stock", "Stock");

    public static final Codec<AttachmentSlot> CODEC = StringRepresentable.fromEnum(AttachmentSlot::values);

    private final String name;
    private final String displayName;

    AttachmentSlot(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }
}