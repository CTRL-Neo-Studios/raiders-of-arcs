package dev.ctrlneo.roa.foundation.utils;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier.GunAttribute;
import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier.ModifierOperation;
import dev.ctrlneo.roa.foundation.data.structures.GunLevelConfig;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GunUtils {

    /**
     * Creates a gun ItemStack in its default state:
     * - Base stats (no attachment modifiers)
     * - Empty magazine
     * - No attachments
     * - Default gun state
     */
    public static ItemStack getDefaultGun(GunItem gunItem) {
        ItemStack stack = new ItemStack(gunItem);

        // Get the base components from the item's default instance
        ItemStack defaultStack = gunItem.getDefaultInstance();

        // Copy base stats
        GunStatsComponent baseStats = defaultStack.get(RoaDataComponents.GUN_STATS.get());
        if (baseStats != null) {
            stack.set(RoaDataComponents.GUN_STATS.get(), baseStats);
        }

        // Set empty magazine with base capacity
        GunMagazineComponent baseMagazine = defaultStack.get(RoaDataComponents.GUN_MAGAZINE.get());
        if (baseMagazine != null) {
            stack.set(RoaDataComponents.GUN_MAGAZINE.get(), new GunMagazineComponent(
                    0,  // Empty
                    baseMagazine.baseCapacity(),
                    baseMagazine.ammoType()
            ));
        }

        // Copy fire modes
        GunFireModesComponent fireModes = defaultStack.get(RoaDataComponents.GUN_FIRE_MODES.get());
        if (fireModes != null) {
            stack.set(RoaDataComponents.GUN_FIRE_MODES.get(), fireModes);
        }

        // Reset state
        stack.set(RoaDataComponents.GUN_STATE.get(), GunStateComponent.DEFAULT);

        // No attachments
        stack.set(RoaDataComponents.GUN_ATTACHMENTS.get(), GunAttachmentsComponent.EMPTY);

        return stack;
    }

    /**
     * Gets a gun with its registered default components (including starting ammo if configured)
     */
    public static ItemStack getGunWithDefaults(GunItem gunItem) {
        // This just returns the default instance which has all components as registered
        return gunItem.getDefaultInstance();
    }

    /**
     * Gets the effective stats of a gun including level bonuses and attachment modifiers.
     * Calculation order:
     * 1. Start with base stats
     * 2. Apply level modifiers (using GunAttributeModifier system)
     * 3. Apply attachment modifiers
     */
    public static GunStatsComponent getEffectiveStats(ItemStack gunStack) {
        GunStatsComponent baseStats = gunStack.get(RoaDataComponents.GUN_STATS.get());
        if (baseStats == null) return GunStatsComponent.DEFAULT;

        // Step 1: Apply level modifiers if gun item supports leveling
        GunStatsComponent statsWithLevel = baseStats;
        if (gunStack.getItem() instanceof GunItem gunItem) {
            GunLevelComponent level = gunStack.get(RoaDataComponents.GUN_LEVEL.get());
            if (level != null && level.currentLevel() > 1) {
                GunLevelConfig levelConfig = gunItem.getLevelConfig();
                List<GunAttributeModifier> levelMods = levelConfig.getCumulativeModifiers(level.currentLevel());
                statsWithLevel = applyAttributeModifiers(baseStats, levelMods);
            }
        }

        // Step 2: Apply attachment modifiers
        GunAttachmentsComponent attachments = gunStack.get(RoaDataComponents.GUN_ATTACHMENTS.get());
        if (attachments == null || !attachments.hasAnyAttachments()) {
            return statsWithLevel;
        }

        AttachmentModifiersComponent combinedMods = attachments.getCombinedModifiers();
        return statsWithLevel.withAttachmentModifiers(combinedMods);
    }

    /**
     * Apply attribute modifiers to gun stats using the unified GunAttributeModifier system.
     * Applies modifiers in standard order: ADD → MULTIPLY_BASE → MULTIPLY_TOTAL
     */
    private static GunStatsComponent applyAttributeModifiers(GunStatsComponent base, List<GunAttributeModifier> modifiers) {
        // Initialize with base values
        float damage = base.damage();
        float accuracy = base.accuracy();
        float recoilVertical = base.recoilVertical();
        float recoilHorizontal = base.recoilHorizontal();
        float fireRate = base.fireRate();
        float range = base.range();
        float armorPenetration = base.armorPenetration();
        float adsSpeed = base.adsSpeed();
        float unholsterSpeed = base.unholsterSpeed();

        // Apply ADD modifiers first
        for (GunAttributeModifier mod : modifiers) {
            if (mod.operation() != ModifierOperation.ADD) continue;
            
            switch (mod.attribute()) {
                case DAMAGE -> damage += mod.value();
                case ACCURACY -> accuracy += mod.value();
                case RECOIL_VERTICAL -> recoilVertical += mod.value();
                case RECOIL_HORIZONTAL -> recoilHorizontal += mod.value();
                case FIRE_RATE -> fireRate += mod.value();
                case RANGE -> range += mod.value();
                case ARMOR_PENETRATION -> armorPenetration += mod.value();
                case ADS_SPEED -> adsSpeed += mod.value();
                case UNHOLSTER_SPEED -> unholsterSpeed += mod.value();
                case MAGAZINE_CAPACITY -> {} // Handled separately in getLevelMagazineBonus()
                case RELOAD_SPEED -> {} // Handled separately in getLevelReloadSpeedBonus()
            }
        }

        // Apply MULTIPLY_BASE modifiers
        for (GunAttributeModifier mod : modifiers) {
            if (mod.operation() != ModifierOperation.MULTIPLY_BASE) continue;
            
            switch (mod.attribute()) {
                case DAMAGE -> damage *= mod.value();
                case ACCURACY -> accuracy *= mod.value();
                case RECOIL_VERTICAL -> recoilVertical *= mod.value();
                case RECOIL_HORIZONTAL -> recoilHorizontal *= mod.value();
                case FIRE_RATE -> fireRate *= mod.value();
                case RANGE -> range *= mod.value();
                case ARMOR_PENETRATION -> armorPenetration *= mod.value();
                case ADS_SPEED -> adsSpeed *= mod.value();
                case UNHOLSTER_SPEED -> unholsterSpeed *= mod.value();
                case MAGAZINE_CAPACITY -> {} // Handled separately in getLevelMagazineBonus()
                case RELOAD_SPEED -> {} // Handled separately in getLevelReloadSpeedBonus()
            }
        }

        // Apply MULTIPLY_TOTAL modifiers
        for (GunAttributeModifier mod : modifiers) {
            if (mod.operation() != ModifierOperation.MULTIPLY_TOTAL) continue;
            
            switch (mod.attribute()) {
                case DAMAGE -> damage *= mod.value();
                case ACCURACY -> accuracy *= mod.value();
                case RECOIL_VERTICAL -> recoilVertical *= mod.value();
                case RECOIL_HORIZONTAL -> recoilHorizontal *= mod.value();
                case FIRE_RATE -> fireRate *= mod.value();
                case RANGE -> range *= mod.value();
                case ARMOR_PENETRATION -> armorPenetration *= mod.value();
                case ADS_SPEED -> adsSpeed *= mod.value();
                case UNHOLSTER_SPEED -> unholsterSpeed *= mod.value();
                case MAGAZINE_CAPACITY -> {} // Handled separately in getLevelMagazineBonus()
                case RELOAD_SPEED -> {} // Handled separately in getLevelReloadSpeedBonus()
            }
        }

        // Note: FOV zoom is no longer in GunStatsComponent, so we keep the base value
        return new GunStatsComponent(
                damage, accuracy, recoilVertical, recoilHorizontal,
                Math.round(fireRate), range, armorPenetration, adsSpeed, unholsterSpeed,
                base.fovZoomMultiplier()  // FOV zoom not modified here
        );
    }

    /**
     * Gets base stats without attachment modifiers
     */
    public static GunStatsComponent getBaseStats(ItemStack gunStack) {
        return gunStack.getOrDefault(RoaDataComponents.GUN_STATS.get(), GunStatsComponent.DEFAULT);
    }

    /**
     * Get the magazine capacity bonus from gun level.
     * This should be added to GunMagazineComponent.getEffectiveCapacity().
     */
    public static int getLevelMagazineBonus(ItemStack gunStack) {
        if (!(gunStack.getItem() instanceof GunItem gunItem)) {
            return 0;
        }

        GunLevelComponent level = gunStack.get(RoaDataComponents.GUN_LEVEL.get());
        if (level == null || level.currentLevel() <= 1) {
            return 0;
        }

        GunLevelConfig levelConfig = gunItem.getLevelConfig();
        List<GunAttributeModifier> levelMods = levelConfig.getCumulativeModifiers(level.currentLevel());
        
        // Sum up all MAGAZINE_CAPACITY modifiers (typically only ADD operations)
        int bonus = 0;
        for (GunAttributeModifier mod : levelMods) {
            if (mod.attribute() == GunAttribute.MAGAZINE_CAPACITY && mod.operation() == ModifierOperation.ADD) {
                bonus += (int) mod.value();
            }
        }
        return bonus;
    }

    /**
     * Get the reload speed bonus from gun level (in seconds, additive).
     * Negative values mean faster reload.
     */
    public static float getLevelReloadSpeedBonus(ItemStack gunStack) {
        if (!(gunStack.getItem() instanceof GunItem gunItem)) {
            return 0.0f;
        }

        GunLevelComponent level = gunStack.get(RoaDataComponents.GUN_LEVEL.get());
        if (level == null || level.currentLevel() <= 1) {
            return 0.0f;
        }

        GunLevelConfig levelConfig = gunItem.getLevelConfig();
        List<GunAttributeModifier> levelMods = levelConfig.getCumulativeModifiers(level.currentLevel());
        
        // Sum up all RELOAD_SPEED modifiers (typically only ADD operations)
        float bonus = 0.0f;
        for (GunAttributeModifier mod : levelMods) {
            if (mod.attribute() == GunAttribute.RELOAD_SPEED && mod.operation() == ModifierOperation.ADD) {
                bonus += mod.value();
            }
        }
        return bonus;
    }

}