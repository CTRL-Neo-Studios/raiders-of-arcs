package dev.ctrlneo.roa.foundation;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.network.packets.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RoaPackets {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Roa.MODID)
                .versioned("1.0")
                .optional();

        // Client -> Server packets
        registrar.playToServer(
                FireGunPacket.TYPE,
                FireGunPacket.STREAM_CODEC,
                FireGunPacket::handle
        );

        registrar.playToServer(
                ReloadGunPacket.TYPE,
                ReloadGunPacket.STREAM_CODEC,
                ReloadGunPacket::handle
        );

        registrar.playToServer(
                CycleFireModePacket.TYPE,
                CycleFireModePacket.STREAM_CODEC,
                CycleFireModePacket::handle
        );

        registrar.playToServer(
                AimDownSightsPacket.TYPE,
                AimDownSightsPacket.STREAM_CODEC,
                AimDownSightsPacket::handle
        );

        registrar.playToServer(
                OpenAttachmentsScreenPacket.TYPE,
                OpenAttachmentsScreenPacket.STREAM_CODEC,
                OpenAttachmentsScreenPacket::handle
        );

        registrar.playToClient(
                ApplyRecoilPacket.TYPE,
                ApplyRecoilPacket.STREAM_CODEC,
                ApplyRecoilPacket::handle
        );
    }

    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static <T extends CustomPacketPayload> void sendToAllPlayers(T packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}