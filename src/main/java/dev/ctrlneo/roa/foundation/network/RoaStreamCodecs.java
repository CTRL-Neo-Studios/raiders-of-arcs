package dev.ctrlneo.roa.foundation.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

public class RoaStreamCodecs {

    /**
     * StreamCodec for InteractionHand
     */
    public static final StreamCodec<ByteBuf, InteractionHand> INTERACTION_HAND = new StreamCodec<>() {
        @Override
        public InteractionHand decode(ByteBuf buffer) {
            return buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        }

        @Override
        public void encode(ByteBuf buffer, InteractionHand hand) {
            buffer.writeBoolean(hand == InteractionHand.MAIN_HAND);
        }
    };
}