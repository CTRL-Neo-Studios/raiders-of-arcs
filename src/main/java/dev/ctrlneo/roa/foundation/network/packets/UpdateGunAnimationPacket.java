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

/**
 * Packet sent from client to server to update gun animation state (idle, aim, sprint).
 * The server then dispatches the appropriate animation command which syncs to all clients.
 */
public record UpdateGunAnimationPacket(InteractionHand hand, AnimationState state) implements CustomPacketPayload {

    public enum AnimationState {
        IDLE,
        AIM,
        SPRINT,
        FIRE,
        AIM_FIRE,
        RELOAD
    }

    public static final CustomPacketPayload.Type<UpdateGunAnimationPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Roa.MODID, "update_gun_animation"));

    public static final StreamCodec<ByteBuf, UpdateGunAnimationPacket> STREAM_CODEC = StreamCodec.composite(
            RoaStreamCodecs.INTERACTION_HAND,
            UpdateGunAnimationPacket::hand,
            ByteBufCodecs.idMapper(i -> AnimationState.values()[i], AnimationState::ordinal),
            UpdateGunAnimationPacket::state,
            UpdateGunAnimationPacket::new
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
                    // Dispatch animation from server side so AzureLib can sync to all clients
                    switch (state) {
                        case IDLE:
                            gunItem.dispatcher.idle(serverPlayer, stack);
                            break;
                        case AIM:
                            gunItem.dispatcher.aim(serverPlayer, stack);
                            break;
                        case SPRINT:
                            gunItem.dispatcher.sprint(serverPlayer, stack);
                            break;
                        case FIRE:
                            gunItem.dispatcher.fire(serverPlayer, stack);
                            break;
                        case AIM_FIRE:
                            gunItem.dispatcher.aimFire(serverPlayer, stack);
                            break;
                        case RELOAD:
                            gunItem.dispatcher.reload(serverPlayer, stack);
                            break;
                    }
                }
            }
        });
    }
}

