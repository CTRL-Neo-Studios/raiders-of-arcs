package dev.ctrlneo.roa.foundation.events;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.GunAttachmentsComponent;
import dev.ctrlneo.roa.foundation.data.components.GunMagazineComponent;
import dev.ctrlneo.roa.foundation.data.components.GunStateComponent;
import dev.ctrlneo.roa.foundation.data.components.GunStatsComponent;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.utils.GunHelper;
import dev.ctrlneo.roa.foundation.utils.GunUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "roa")
public class ServerReloadHandler {

    private static final java.util.Map<java.util.UUID, ItemStack> playerReloadingGuns = new java.util.HashMap<>();

    @SubscribeEvent
    public static void onServerPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandItem();

        if (!(mainHandStack.getItem() instanceof GunItem)) {
            // Cancel reload if switched away
            ItemStack reloadingGun = playerReloadingGuns.get(player.getUUID());
            if (reloadingGun != null) {
                cancelReload(reloadingGun, player);
                playerReloadingGuns.remove(player.getUUID());
            }
            return;
        }

        GunStateComponent state = mainHandStack.get(RoaDataComponents.GUN_STATE.get());
        if (state == null || !state.isReloading()) {
            playerReloadingGuns.remove(player.getUUID());
            return;
        }

        // Track reloading gun
        playerReloadingGuns.put(player.getUUID(), mainHandStack.copy());

        // Check reload completion
        GunStatsComponent stats = GunUtils.getEffectiveStats(mainHandStack);
        long currentTime = player.level().getGameTime();

        if (state.isReloadComplete(currentTime, stats.getReloadTicks())) {
            completeReload(mainHandStack, player);
        }
    }

    private static void completeReload(ItemStack gunStack, ServerPlayer player) {
        GunAttachmentsComponent attachments = gunStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        GunStateComponent state = gunStack.get(RoaDataComponents.GUN_STATE.get());

        // Perform the actual reload
        GunHelper.reload(gunStack, player, attachments);
        gunStack.set(RoaDataComponents.GUN_STATE.get(), state.completeReload());

        // Play completion sound
        player.playSound(SoundEvents.PISTON_CONTRACT, 0.8f, 1.0f);

        // Show message
        GunMagazineComponent magazine = gunStack.get(RoaDataComponents.GUN_MAGAZINE.get());
        if (magazine != null) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("gui.roa.reloaded",
                            magazine.currentAmmo(),
                            magazine.getEffectiveCapacity(attachments)),
                    true
            );
        }
    }

    private static void cancelReload(ItemStack gunStack, ServerPlayer player) {
        GunStateComponent state = gunStack.get(RoaDataComponents.GUN_STATE.get());
        if (state != null && state.isReloading()) {
            gunStack.set(RoaDataComponents.GUN_STATE.get(), state.cancelReload());
            player.playSound(SoundEvents.ITEM_BREAK, 0.5f, 1.2f);
        }
    }
}