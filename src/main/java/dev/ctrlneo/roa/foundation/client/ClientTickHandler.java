package dev.ctrlneo.roa.foundation.client;

import dev.ctrlneo.roa.RoaConfig;
import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.RoaKeybinds;
import dev.ctrlneo.roa.foundation.RoaPackets;
import dev.ctrlneo.roa.foundation.data.components.GunFireModesComponent;
import dev.ctrlneo.roa.foundation.data.components.GunStatsComponent;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.packets.AimDownSightsPacket;
import dev.ctrlneo.roa.foundation.network.packets.CycleFireModePacket;
import dev.ctrlneo.roa.foundation.network.packets.FireGunPacket;
import dev.ctrlneo.roa.foundation.network.packets.OpenAttachmentsScreenPacket;
import dev.ctrlneo.roa.foundation.network.packets.ReloadGunPacket;
import dev.ctrlneo.roa.foundation.utils.GunUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            // Reset ADS state if not holding a gun
            AdsStateManager.reset();
            return;
        }

        // Update ADS transition speed based on gun stats
        GunStatsComponent stats = GunUtils.getEffectiveStats(mainHandStack);
        if (stats != null) {
            AdsStateManager.setTransitionSpeed(stats.adsSpeed());
        }

        // Update ADS progress for smooth transitions
        AdsStateManager.updateAdsProgress();

        // Handle ADS hold mode
        handleAdsHoldMode(mc, mainHandStack);

        // Handle automatic fire
        handleAutomaticFire(mc, player, mainHandStack);

        // Handle keybinds
        handleKeybinds(player);
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
    }

    private static void handleKeybinds(LocalPlayer player) {
        // Reload (R)
        while (RoaKeybinds.RELOAD.consumeClick()) {
            RoaPackets.sendToServer(new ReloadGunPacket(InteractionHand.MAIN_HAND));
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