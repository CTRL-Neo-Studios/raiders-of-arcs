package dev.ctrlneo.roa.foundation.network.packets;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.RoaStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FireGunPacket(InteractionHand hand, boolean pressed) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FireGunPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Roa.MODID, "fire_gun"));

    public static final StreamCodec<ByteBuf, FireGunPacket> STREAM_CODEC = StreamCodec.composite(
            RoaStreamCodecs.INTERACTION_HAND,
            FireGunPacket::hand,
            ByteBufCodecs.BOOL,
            FireGunPacket::pressed,
            FireGunPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                ItemStack stack = serverPlayer.getItemInHand(hand);
                if (stack.getItem() instanceof GunItem gunItem) {
                    if (pressed) {
                        gunItem.tryFire(serverPlayer.level(), serverPlayer, stack);
                    }
                }
            }
        });
    }
}