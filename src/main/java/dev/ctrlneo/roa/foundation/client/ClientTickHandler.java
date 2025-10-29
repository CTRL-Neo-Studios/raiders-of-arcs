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
        
        // Check if player just switched to this gun (trigger unholster animation)
        boolean justSwitchedToGun = !ItemStack.isSameItem(mainHandStack, prevMainHandStack) || 
                                     !(prevMainHandStack.getItem() instanceof GunItem);
        if (justSwitchedToGun) {
            triggerUnholster(mainHandStack, player, gunItem);
        }

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
        
        // Prevent sprinting while aiming (fixes rapid sprint/aim switching when holding sprint key)
        if (AdsStateManager.isPlayerAiming() && player.isSprinting()) {
            player.setSprinting(false);
        }

        // Check reload completion
        checkReloadCompletion(mainHandStack, player);
        
        // Check unholster completion
        checkUnholsterCompletion(mainHandStack, player, gunItem);

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
    
    private static void triggerUnholster(ItemStack gunStack, LocalPlayer player, GunItem gunItem) {
        GunStateComponent state = gunStack.get(RoaDataComponents.GUN_STATE.get());
        if (state == null) return;
        
        // Start unholster
        long currentTime = player.level().getGameTime();
        gunStack.set(RoaDataComponents.GUN_STATE.get(), state.withUnholstering(true, currentTime));
        
        // Play unholster sound (optional)
        player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 0.8f, 1.0f);
    }
    
    private static void checkUnholsterCompletion(ItemStack gunStack, LocalPlayer player, GunItem gunItem) {
        GunStateComponent state = gunStack.get(RoaDataComponents.GUN_STATE.get());
        if (state == null || !state.isUnholstering()) {
            return;
        }
        
        GunStatsComponent stats = GunUtils.getEffectiveStats(gunStack);
        long currentTime = player.level().getGameTime();
        
        // Check if unholster is complete
        if (state.isUnholsterComplete(currentTime, stats.getUnholsterTicks())) {
            // Complete unholster
            gunStack.set(RoaDataComponents.GUN_STATE.get(), state.completeUnholster());
            
            // Play ready sound (optional)
            player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 0.6f, 1.2f);
        }
    }

    private static void checkReloadCompletion(ItemStack gunStack, LocalPlayer player) {
        GunStateComponent state = gunStack.get(RoaDataComponents.GUN_STATE.get());
        if (state == null || !state.isReloading()) {
            return;
        }

        GunReloadComponent reloadConfig = gunStack.get(RoaDataComponents.GUN_RELOAD.get());
        if (reloadConfig == null) return;
        
        long currentTime = player.level().getGameTime();
        
        // Note: The actual reload logic (adding ammo) happens server-side in GunItem.inventoryTick
        // This client-side check is mainly for animation transitions and sound feedback
        
        if (reloadConfig.reloadType() == dev.ctrlneo.roa.foundation.data.structures.ReloadType.ONE_SHOT) {
            // One-shot reload: check if full reload is complete
            if (state.isReloadComplete(currentTime, reloadConfig.getReloadDurationTicks())) {
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
                                    magazine.getEffectiveCapacity(gunStack, attachments)),
                            true);
                }

                // Transition animation back to appropriate state
                // The GunAnimationStateManager will pick the right animation on next tick
            }
        } else {
            // Sequential reload: client-side only handles animation transitions
            // The server handles the actual ammo addition in GunItem.inventoryTick
            // No client-side action needed here - server will sync the state
        }
    }

    private static void handleAdsHoldMode(Minecraft mc, ItemStack gunStack) {
        // Only check hold mode (toggle mode is handled by the initial click)
        if (RoaConfig.CLIENT.toggleAds.get()) {
            return;
        }

        // Check if ADS keybind is released (allows players to rebind it!)
        if (!RoaKeybinds.AIM_DOWN_SIGHTS.isDown() && AdsStateManager.isPlayerAiming()) {
            AdsStateManager.setAiming(false);
            RoaPackets.sendToServer(new AimDownSightsPacket(InteractionHand.MAIN_HAND, false));
        }
        
        // Also cancel ADS if player starts reloading
        GunStateComponent gunState = gunStack.get(RoaDataComponents.GUN_STATE.get());
        if (gunState != null && gunState.isReloading() && AdsStateManager.isPlayerAiming()) {
            AdsStateManager.setAiming(false);
            RoaPackets.sendToServer(new AimDownSightsPacket(InteractionHand.MAIN_HAND, false));
        }
    }

    private static void handleAutomaticFire(Minecraft mc, LocalPlayer player, ItemStack gunStack) {
        // Check if attack key is being held (use vanilla attack key for shooting)
        // This allows compatibility with vanilla controls
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

        // Check if gun has ammo before playing animation
        GunMagazineComponent magazine = gunStack.get(RoaDataComponents.GUN_MAGAZINE.get());
        boolean hasAmmo = magazine != null && magazine.currentAmmo() > 0;

        // Send fire packet continuously
        RoaPackets.sendToServer(new FireGunPacket(InteractionHand.MAIN_HAND, true));
        
        // Only play fire animation if gun has ammo
        if (hasAmmo) {
            boolean isAiming = AdsStateManager.isPlayerAiming();
            if (gunStack.getItem() instanceof GunItem gunItem) {
                GunAnimationStateManager.notifyFire(player, gunStack, gunItem, isAiming);
            }
        }
    }

    private static void handleKeybinds(LocalPlayer player) {
        ItemStack mainHandStack = player.getMainHandItem();
        
        // ADS (Right Mouse Button by default, but rebindable!)
        while (RoaKeybinds.AIM_DOWN_SIGHTS.consumeClick()) {
            if (mainHandStack.getItem() instanceof GunItem) {
                // Check if currently reloading - can't aim while reloading
                GunStateComponent gunState = mainHandStack.get(RoaDataComponents.GUN_STATE.get());
                if (gunState != null && gunState.isReloading()) {
                    continue;
                }
                
                boolean toggleAds = RoaConfig.CLIENT.toggleAds.get();
                boolean newAiming;

                if (toggleAds) {
                    // Toggle mode - flip the state
                    newAiming = !AdsStateManager.isPlayerAiming();
                } else {
                    // Hold mode - always set to true on press
                    // Release is handled in handleAdsHoldMode
                    newAiming = true;
                }

                AdsStateManager.setAiming(newAiming);
                RoaPackets.sendToServer(new AimDownSightsPacket(InteractionHand.MAIN_HAND, newAiming));
                
                // Cancel sprint when starting to aim
                if (newAiming && player.isSprinting()) {
                    player.setSprinting(false);
                }
            }
        }
        
        // Reload (R)
        while (RoaKeybinds.RELOAD.consumeClick()) {
            RoaPackets.sendToServer(new ReloadGunPacket(InteractionHand.MAIN_HAND));
            
            // Notify animation system that reload animation should play
            if (mainHandStack.getItem() instanceof GunItem gunItem) {
                GunAnimationStateManager.notifyReload(player, mainHandStack, gunItem);
            }
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
