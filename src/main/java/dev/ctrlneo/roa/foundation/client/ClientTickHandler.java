package dev.ctrlneo.roa.foundation.client;

import dev.ctrlneo.roa.RoaConfig;
import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.RoaKeybinds;
import dev.ctrlneo.roa.foundation.RoaPackets;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.packets.AimDownSightsPacket;
import dev.ctrlneo.roa.foundation.network.packets.CycleFireModePacket;
import dev.ctrlneo.roa.foundation.network.packets.FireGunPacket;
import dev.ctrlneo.roa.foundation.network.packets.OpenAttachmentsScreenPacket;
import dev.ctrlneo.roa.foundation.network.packets.ReloadGunPacket;
import dev.ctrlneo.roa.foundation.utils.GunHelper;
import dev.ctrlneo.roa.foundation.utils.GunUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "roa", value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack prevMainHandStack = getPreviousMainHandStack(player);

        if (!(mainHandStack.getItem() instanceof GunItem)) {
            // Reset ADS state if not holding a gun
            AdsStateManager.reset();
            RecoilManager.reset(); // Reset recoil too
            GunAnimationStateManager.reset(); // Reset animation state too!

            // Cancel reload if switched away from gun
            if (prevMainHandStack.getItem() instanceof GunItem) {
                cancelReloadIfSwitched(prevMainHandStack, player);
            }

            return;
        }

        // At this point we know we're holding a gun
        GunItem gunItem = (GunItem) mainHandStack.getItem();

        // Check if we switched items while reloading
        if (!ItemStack.isSameItem(mainHandStack, prevMainHandStack)) {
            cancelReloadIfSwitched(prevMainHandStack, player);
        }

        // Update ADS transition speed
        GunStatsComponent stats = GunUtils.getEffectiveStats(mainHandStack);
        if (stats != null) {
            AdsStateManager.setTransitionSpeed(stats.adsSpeed());
        }

        // Update ADS progress
        AdsStateManager.updateAdsProgress();

        // Update recoil (CLIENT-SIDE)
        RecoilManager.updateRecoil();

        // Update gun animations based on player state
        GunAnimationStateManager.updateAnimationState(player, mainHandStack, gunItem);

        // Check reload completion
        checkReloadCompletion(mainHandStack, player);

        // Handle ADS hold mode
        handleAdsHoldMode(mc, mainHandStack);

        // Handle automatic fire
        handleAutomaticFire(mc, player, mainHandStack);

        // Handle keybinds
        handleKeybinds(player);

        // Store current item for next tick
        storePreviousMainHandStack(player, mainHandStack);
    }

    private static ItemStack previousMainHandStack = ItemStack.EMPTY;

    private static ItemStack getPreviousMainHandStack(LocalPlayer player) {
        return previousMainHandStack;
    }

    private static void storePreviousMainHandStack(LocalPlayer player, ItemStack stack) {
        previousMainHandStack = stack.copy();
    }

    private static void cancelReloadIfSwitched(ItemStack gunStack, LocalPlayer player) {
        GunStateComponent state = gunStack.get(RoaDataComponents.GUN_STATE.get());
        if (state != null && state.isReloading()) {
            // Cancel reload
            gunStack.set(RoaDataComponents.GUN_STATE.get(), state.cancelReload());
            player.playSound(SoundEvents.ITEM_BREAK, 0.5f, 1.2f);
        }
    }

    private static void checkReloadCompletion(ItemStack gunStack, LocalPlayer player) {
        GunStateComponent state = gunStack.get(RoaDataComponents.GUN_STATE.get());
        if (state == null || !state.isReloading()) {
            return;
        }

        GunStatsComponent stats = GunUtils.getEffectiveStats(gunStack);
        long currentTime = player.level().getGameTime();

        // Check if reload is complete
        if (state.isReloadComplete(currentTime, stats.getReloadTicks())) {
            // Complete the reload
            GunAttachmentsComponent attachments = gunStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
            GunHelper.reload(gunStack, player, attachments);
            gunStack.set(RoaDataComponents.GUN_STATE.get(), state.completeReload());

            // Play reload complete sound
            player.playSound(SoundEvents.PISTON_CONTRACT, 0.8f, 1.0f);

            // Show message
            GunMagazineComponent magazine = gunStack.get(RoaDataComponents.GUN_MAGAZINE.get());
            if (magazine != null) {
                player.displayClientMessage(
                        Component.translatable("gui.roa.reloaded",
                                magazine.currentAmmo(),
                                magazine.getEffectiveCapacity(attachments)),
                        true);
            }

            // Transition animation back to appropriate state
            // The GunAnimationStateManager will pick the right animation on next tick
            // (idle, aim, or sprint depending on what player is doing)
        }
    }

    private static void handleAdsHoldMode(Minecraft mc, ItemStack gunStack) {
        // Only check hold mode (toggle mode is handled by the initial click)
        if (RoaConfig.CLIENT.toggleAds.get()) {
            return;
        }

        // Check if right mouse button is released
        if (!mc.options.keyUse.isDown() && AdsStateManager.isPlayerAiming()) {
            AdsStateManager.setAiming(false);
            RoaPackets.sendToServer(new AimDownSightsPacket(InteractionHand.MAIN_HAND, false));
        }
    }

    private static void handleAutomaticFire(Minecraft mc, LocalPlayer player, ItemStack gunStack) {
        // Check if left mouse button is being held
        if (!mc.options.keyAttack.isDown()) {
            return;
        }

        // Check if gun is in automatic fire mode
        GunFireModesComponent modes = gunStack.get(RoaDataComponents.GUN_FIRE_MODES.get());
        if (modes == null || modes.currentMode() != GunFireMode.AUTOMATIC_FIRE) {
            return;
        }

        // Don't fire if a screen is open
        if (mc.screen != null) {
            return;
        }

        // Send fire packet continuously
        RoaPackets.sendToServer(new FireGunPacket(InteractionHand.MAIN_HAND, true));
        
        // Notify animation system that a PLAY_ONCE fire animation will play
        // Fire animations typically take ~10 ticks (500ms)
        GunAnimationStateManager.notifyPlayOnceAnimation(player, 10);
    }

    private static void handleKeybinds(LocalPlayer player) {
        // Reload (R)
        while (RoaKeybinds.RELOAD.consumeClick()) {
            RoaPackets.sendToServer(new ReloadGunPacket(InteractionHand.MAIN_HAND));
            
            // Notify animation system that a PLAY_ONCE reload animation will play
            // Reload animations typically take ~80-100 ticks (4-5 seconds)
            GunAnimationStateManager.notifyPlayOnceAnimation(player, 100);
        }

        // Fire Mode Cycle (B)
        while (RoaKeybinds.FIRE_MODE_CYCLE.consumeClick()) {
            RoaPackets.sendToServer(new CycleFireModePacket(InteractionHand.MAIN_HAND));
        }

        // Open Attachments UI (I)
        while (RoaKeybinds.OPEN_ATTACHMENTS.consumeClick()) {
            RoaPackets.sendToServer(new OpenAttachmentsScreenPacket(InteractionHand.MAIN_HAND));
        }
    }
}
