package dev.ctrlneo.roa.foundation.utils;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.structures.AmmoType;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.items.GunItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

/**
 * Helper class for registering guns with a fluent builder pattern.
 *
 * Usage:
 * <pre>
 * public static final DeferredItem<GunItem> ASSAULT_RIFLE = GunRegistryHelper.gun("assault_rifle")
 *     .stats(8.0f, 0.85f, 1.2f, 0.8f, 650, 60.0f, 0.1f)
 *     .ammo(AmmoType.MEDIUM, 30)
 *     .fireModes(GunFireMode.SINGLE_FIRE, GunFireMode.BURST3_FIRE, GunFireMode.AUTOMATIC_FIRE)
 *     .register();
 * </pre>
 */
public class GunRegistryHelper {

    /**
     * Creates a new gun builder with the given registry name
     */
    public static GunBuilder gun(String name) {
        return new GunBuilder(name);
    }

    public static class GunBuilder {
        private final String name;

        // Default values
        private GunStatsComponent stats = GunStatsComponent.DEFAULT;
        private AmmoType ammoType = AmmoType.MEDIUM;
        private int magazineCapacity = 30;
        private List<GunFireMode> availableFireModes = List.of(GunFireMode.SINGLE_FIRE);
        private GunFireMode defaultFireMode = GunFireMode.SINGLE_FIRE;
        private boolean startsLoaded = true;

        private GunBuilder(String name) {
            this.name = name;
        }

        /**
         * Sets the gun's statistics
         * @param damage Base damage per shot
         * @param accuracy Accuracy (0.0 - 1.0, where 1.0 is perfect)
         * @param recoilVertical Vertical recoil multiplier
         * @param recoilHorizontal Horizontal recoil multiplier
         * @param fireRate Rounds per minute
         * @param range Effective range in blocks
         * @param penetration Armor penetration (0.0 - 1.0)
         * @param adsSpeed Time needed to fully enter/exit aim down sight, in seconds
         * @param unholsterSpeed The time it takes to take the gun out, in seconds
         * @param reloadSpeed The time it takes to reload the gun, in seconds
         */
        public GunBuilder stats(float damage, float accuracy, float recoilVertical,
                                float recoilHorizontal, int fireRate, float range,
                                float penetration, float adsSpeed, float unholsterSpeed,
                                float reloadSpeed) {
            this.stats = new GunStatsComponent(
                    damage, accuracy, recoilVertical, recoilHorizontal,
                    fireRate, range, penetration, adsSpeed, unholsterSpeed, reloadSpeed
            );
            return this;
        }

        /**
         * Sets the gun's statistics using a pre-built GunStatsComponent
         */
        public GunBuilder stats(GunStatsComponent stats) {
            this.stats = stats;
            return this;
        }

        /**
         * Sets the ammo type and magazine capacity
         */
        public GunBuilder ammo(AmmoType type, int capacity) {
            this.ammoType = type;
            this.magazineCapacity = capacity;
            return this;
        }

        /**
         * Sets available fire modes. The first mode will be the default.
         */
        public GunBuilder fireModes(GunFireMode... modes) {
            if (modes.length == 0) {
                throw new IllegalArgumentException("Must provide at least one fire mode");
            }
            this.availableFireModes = List.of(modes);
            this.defaultFireMode = modes[0];
            return this;
        }

        /**
         * Makes the gun start with an empty magazine (useful for high-tier weapons)
         */
        public GunBuilder startsEmpty() {
            this.startsLoaded = false;
            return this;
        }

        /**
         * Makes the gun start with a loaded magazine (default behavior)
         */
        public GunBuilder startsLoaded() {
            this.startsLoaded = true;
            return this;
        }

        /**
         * Registers the gun with all configured components
         */
        public DeferredItem<GunItem> register() {
            return Roa.ITEMS.register(name, () -> new GunItem(
                    new Item.Properties()
                            .component(RoaDataComponents.GUN_STATS.get(), stats)
                            .component(RoaDataComponents.GUN_MAGAZINE.get(), new GunMagazineComponent(
                                    startsLoaded ? magazineCapacity : 0,
                                    magazineCapacity,
                                    ammoType.getHolder()
                            ))
                            .component(RoaDataComponents.GUN_FIRE_MODES.get(), new GunFireModesComponent(
                                    defaultFireMode,
                                    availableFireModes
                            ))
                            .component(RoaDataComponents.GUN_STATE.get(), GunStateComponent.DEFAULT)
                            .component(RoaDataComponents.GUN_ATTACHMENTS.get(), GunAttachmentsComponent.EMPTY)
            ));
        }
    }

    // Preset builders for common gun archetypes

    /**
     * Creates a pistol with sensible defaults
     */
    public static GunBuilder pistol(String name) {
        return gun(name)
                .stats(6.0f, 0.90f, 0.8f, 0.5f, 400, 40.0f, 0.05f)
                .ammo(AmmoType.LIGHT, 15)
                .fireModes(GunFireMode.SINGLE_FIRE);
    }

    /**
     * Creates an assault rifle with sensible defaults
     */
    public static GunBuilder assaultRifle(String name) {
        return gun(name)
                .stats(8.0f, 0.85f, 1.2f, 0.8f, 650, 60.0f, 0.1f)
                .ammo(AmmoType.MEDIUM, 30)
                .fireModes(GunFireMode.SINGLE_FIRE, GunFireMode.BURST3_FIRE, GunFireMode.AUTOMATIC_FIRE);
    }

    /**
     * Creates a sniper rifle with sensible defaults
     */
    public static GunBuilder sniperRifle(String name) {
        return gun(name)
                .stats(25.0f, 0.98f, 2.5f, 1.5f, 60, 150.0f, 0.5f)
                .ammo(AmmoType.HEAVY, 5)
                .fireModes(GunFireMode.SINGLE_FIRE);
    }

    /**
     * Creates an SMG with sensible defaults
     */
    public static GunBuilder smg(String name) {
        return gun(name)
                .stats(5.0f, 0.75f, 0.6f, 0.4f, 900, 30.0f, 0.0f)
                .ammo(AmmoType.LIGHT, 35)
                .fireModes(GunFireMode.AUTOMATIC_FIRE, GunFireMode.BURST2_FIRE);
    }

    /**
     * Creates a shotgun with sensible defaults
     */
    public static GunBuilder shotgun(String name) {
        return gun(name)
                .stats(12.0f, 0.60f, 1.8f, 1.2f, 120, 20.0f, 0.0f)
                .ammo(AmmoType.SHOTGUN, 8)
                .fireModes(GunFireMode.SINGLE_FIRE);
    }

    /**
     * Creates a launcher with sensible defaults
     */
    public static GunBuilder launcher(String name) {
        return gun(name)
                .stats(40.0f, 0.95f, 3.0f, 2.0f, 30, 80.0f, 0.8f)
                .ammo(AmmoType.LAUNCHER, 1)
                .fireModes(GunFireMode.SINGLE_FIRE)
                .startsEmpty();
    }

    /**
     * Creates an energy weapon with sensible defaults
     */
    public static GunBuilder energyWeapon(String name) {
        return gun(name)
                .stats(10.0f, 0.95f, 0.5f, 0.3f, 800, 70.0f, 0.3f)
                .ammo(AmmoType.ENERGY, 50)
                .fireModes(GunFireMode.AUTOMATIC_FIRE);
    }
}