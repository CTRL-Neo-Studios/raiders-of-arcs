package dev.ctrlneo.roa.foundation.network.packets;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.RoaStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenAttachmentsScreenPacket(InteractionHand hand) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenAttachmentsScreenPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Roa.MODID, "open_attachments_screen"));

    public static final StreamCodec<ByteBuf, OpenAttachmentsScreenPacket> STREAM_CODEC = StreamCodec.composite(
            RoaStreamCodecs.INTERACTION_HAND,
            OpenAttachmentsScreenPacket::hand,
            OpenAttachmentsScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ItemStack stack = serverPlayer.getItemInHand(hand);
                if (stack.getItem() instanceof GunItem) {
                    // TODO: Open attachment screen
                    // serverPlayer.openMenu(new AttachmentMenuProvider(hand));
                }
            }
        });
    }
}