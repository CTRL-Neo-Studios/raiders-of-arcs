package dev.ctrlneo.roa.foundation.network.packets;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.network.RoaStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AimDownSightsPacket(InteractionHand hand, boolean aiming) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AimDownSightsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Roa.MODID, "aim_down_sights"));

    public static final StreamCodec<ByteBuf, AimDownSightsPacket> STREAM_CODEC = StreamCodec.composite(
            RoaStreamCodecs.INTERACTION_HAND,
            AimDownSightsPacket::hand,
            ByteBufCodecs.BOOL,
            AimDownSightsPacket::aiming,
            AimDownSightsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // You can store ADS state server-side if needed for animations
            // For now, this is mainly client-authoritative
        });
    }
}