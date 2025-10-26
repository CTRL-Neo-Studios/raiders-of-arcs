package dev.ctrlneo.roa.foundation.network.packets;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.client.RecoilManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ApplyRecoilPacket(float pitchRecoil, float yawRecoil) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ApplyRecoilPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Roa.MODID, "apply_recoil"));

    public static final StreamCodec<ByteBuf, ApplyRecoilPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            ApplyRecoilPacket::pitchRecoil,
            ByteBufCodecs.FLOAT,
            ApplyRecoilPacket::yawRecoil,
            ApplyRecoilPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client-side only
            if (context.flow().isClientbound()) {
                RecoilManager.addRecoil(pitchRecoil, yawRecoil);
            }
        });
    }
}