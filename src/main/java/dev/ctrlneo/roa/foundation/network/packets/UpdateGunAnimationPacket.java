package dev.ctrlneo.roa.foundation.network.packets;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.RoaStreamCodecs;
import io.netty.buffer.ByteBuf;
import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet sent from client to server to update gun animation state.
 * Now properly integrates with AnimatorController to send animation name AND play behavior.
 */
public record UpdateGunAnimationPacket(
        InteractionHand hand, 
        String animationName, 
        int playBehaviorOrdinal
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateGunAnimationPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Roa.MODID, "update_gun_animation"));

    public static final StreamCodec<ByteBuf, UpdateGunAnimationPacket> STREAM_CODEC = StreamCodec.composite(
            RoaStreamCodecs.INTERACTION_HAND,
            UpdateGunAnimationPacket::hand,
            ByteBufCodecs.STRING_UTF8,
            UpdateGunAnimationPacket::animationName,
            ByteBufCodecs.VAR_INT,
            UpdateGunAnimationPacket::playBehaviorOrdinal,
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
                if (stack.getItem() instanceof GunItem) {
                    // Get the play behavior from ordinal
                    AzPlayBehavior playBehavior = getPlayBehaviorFromOrdinal(playBehaviorOrdinal);
                    
                    // Create and dispatch the animation command dynamically
                    // This now properly respects the AnimatorController's play behavior!
                    AzCommand command = AzCommand.create("base_controller", animationName, playBehavior);
                    command.sendForItem(serverPlayer, stack);
                }
            }
        });
    }
    
    /**
     * Convert ordinal back to AzPlayBehavior.
     * AzureLib doesn't expose ordinals, so we manually map them.
     */
    private static AzPlayBehavior getPlayBehaviorFromOrdinal(int ordinal) {
        return switch (ordinal) {
            case 0 -> AzPlayBehaviors.LOOP;
            case 1 -> AzPlayBehaviors.PLAY_ONCE;
            case 2 -> AzPlayBehaviors.HOLD_ON_LAST_FRAME;
            default -> AzPlayBehaviors.LOOP; // Safe default
        };
    }
}

