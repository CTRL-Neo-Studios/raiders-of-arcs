package dev.ctrlneo.roa.foundation.data.components;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * Describes stat modifications provided by an attachment.
 * Uses the unified GunAttributeModifier system for consistency with gun levels.
 */
public record AttachmentModifiersComponent(
        List<GunAttributeModifier> modifiers
) {
    public static final AttachmentModifiersComponent EMPTY = new AttachmentModifiersComponent(List.of());

    // Codec for JSON/NBT serialization
    public static final Codec<AttachmentModifiersComponent> CODEC =
            GunAttributeModifier.CODEC.listOf()
                    .xmap(AttachmentModifiersComponent::new, AttachmentModifiersComponent::modifiers);

    // StreamCodec for network synchronization
    public static final StreamCodec<ByteBuf, AttachmentModifiersComponent> STREAM_CODEC =
            GunAttributeModifier.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(AttachmentModifiersComponent::new, AttachmentModifiersComponent::modifiers);

    /**
     * Combines multiple modifiers (for stacking multiple attachments).
     * Simply concatenates the modifier lists.
     */
    public AttachmentModifiersComponent combine(AttachmentModifiersComponent other) {
        List<GunAttributeModifier> combined = new java.util.ArrayList<>(this.modifiers);
        combined.addAll(other.modifiers);
        return new AttachmentModifiersComponent(List.copyOf(combined));
    }

    /**
     * Checks if this modifier actually modifies anything
     */
    public boolean isEmpty() {
        return modifiers.isEmpty();
    }
}