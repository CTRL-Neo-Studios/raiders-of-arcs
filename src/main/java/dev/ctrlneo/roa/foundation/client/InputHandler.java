package dev.ctrlneo.roa.foundation.client;

import dev.ctrlneo.roa.foundation.RoaKeybinds;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.network.RoaPackets;
import dev.ctrlneo.roa.foundation.network.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "roa", value = Dist.CLIENT)
public class InputHandler {

    private static boolean wasAdsPressed = false;
    private static boolean isAiming = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        handleKeyBinds();
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;

        ItemStack mainHandStack = player.getMainHandItem();

        // Only handle if holding a gun
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            return;
        }

        // Left Mouse Button (Fire)
        if (event.getButton() == 0 && event.getAction() == 1) { // 0 = LMB, 1 = Press
            event.setCanceled(true); // Prevent vanilla attack
            RoaPackets.sendToServer(new FireGunPacket(InteractionHand.MAIN_HAND, true));
        }

        // Right Mouse Button (ADS)
        if (event.getButton() == 1) { // 1 = RMB
            event.setCanceled(true); // Prevent vanilla use
            handleADS(event.getAction() == 1); // 1 = Press, 0 = Release
        }
    }

    @SubscribeEvent
    public static void onClientTick(PlayerTickEvent.Post event) {
        handleKeyBinds();
    }

    private static void handleKeyBinds() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;

        ItemStack mainHandStack = player.getMainHandItem();
        if (!(mainHandStack.getItem() instanceof GunItem)) {
            isAiming = false;
            return;
        }

        // Reload (R)
        if (RoaKeybinds.RELOAD.consumeClick()) {
            RoaPackets.sendToServer(new ReloadGunPacket(InteractionHand.MAIN_HAND));
        }

        // Fire Mode Cycle (B)
        if (RoaKeybinds.FIRE_MODE_CYCLE.consumeClick()) {
            RoaPackets.sendToServer(new CycleFireModePacket(InteractionHand.MAIN_HAND));
        }

        // Open Attachments UI (I)
        if (RoaKeybinds.OPEN_ATTACHMENTS.consumeClick()) {
            RoaPackets.sendToServer(new OpenAttachmentsScreenPacket(InteractionHand.MAIN_HAND));
        }
    }

    private static void handleADS(boolean pressed) {
        boolean toggleAds = RoaConfig.CLIENT.toggleAds.get();

        if (toggleAds) {
            // Toggle mode: press once to enable, press again to disable
            if (pressed && !wasAdsPressed) {
                isAiming = !isAiming;
                RoaPackets.sendToServer(new AimDownSightsPacket(InteractionHand.MAIN_HAND, isAiming));
            }
            wasAdsPressed = pressed;
        } else {
            // Hold mode: hold to ADS, release to stop
            if (isAiming != pressed) {
                isAiming = pressed;
                RoaPackets.sendToServer(new AimDownSightsPacket(InteractionHand.MAIN_HAND, isAiming));
            }
        }
    }

    public static boolean isAiming() {
        return isAiming;
    }

    public static void setAiming(boolean aiming) {
        isAiming = aiming;
    }
}