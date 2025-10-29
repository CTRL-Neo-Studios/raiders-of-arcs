package dev.ctrlneo.roa.foundation.utils;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.GunAttachmentsComponent;
import dev.ctrlneo.roa.foundation.data.components.GunMagazineComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GunHelper {

    public static boolean canReload(ItemStack gunStack, Player player, GunAttachmentsComponent attachments) {
        GunMagazineComponent magazine = gunStack.get(RoaDataComponents.GUN_MAGAZINE.get());
        if (magazine == null || magazine.isFull(attachments)) return false;

        // Check if player has ammo
        return player.getInventory().countItem(magazine.ammoType().value()) > 0;
    }

    public static void reload(ItemStack gunStack, Player player, GunAttachmentsComponent attachments) {
        GunMagazineComponent magazine = gunStack.get(RoaDataComponents.GUN_MAGAZINE.get());
        if (magazine == null) return;

        int effectiveCapacity = magazine.getEffectiveCapacity(gunStack, attachments);
        int needed = effectiveCapacity - magazine.currentAmmo();
        int available = player.getInventory().countItem(magazine.ammoType().value());
        int toReload = Math.min(needed, available);

        if (toReload > 0) {
            // Remove ammo from inventory
            player.getInventory().clearOrCountMatchingItems(
                    stack -> stack.is(magazine.ammoType().value()),
                    toReload,
                    player.inventoryMenu.getCraftSlots()
            );

            // Update magazine
            gunStack.set(RoaDataComponents.GUN_MAGAZINE.get(),
                    magazine.reload(toReload, attachments));
        }
    }

    public static int getAmmoInInventory(Player player, ItemStack gunStack) {
        GunMagazineComponent magazine = gunStack.get(RoaDataComponents.GUN_MAGAZINE.get());
        if (magazine == null) return 0;

        return player.getInventory().countItem(magazine.ammoType().value());
    }
}