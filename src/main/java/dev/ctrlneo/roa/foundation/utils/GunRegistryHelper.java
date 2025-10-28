package dev.ctrlneo.roa.foundation.utils;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.RoaItemRenderers;
import dev.ctrlneo.roa.foundation.animations.controller.AnimatorController;
import dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController;
import dev.ctrlneo.roa.foundation.client.renderer.GunItemRenderer;
import dev.ctrlneo.roa.foundation.data.components.*;
import dev.ctrlneo.roa.foundation.data.structures.AmmoType;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.items.GunItem;
import mod.azure.azurelib.common.render.item.AzItemRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

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
        private Set<GunFireMode> availableFireModes = Set.of(GunFireMode.SINGLE_FIRE);
        private GunFireMode defaultFireMode = GunFireMode.SINGLE_FIRE;
        private boolean startsLoaded = true;
        private int durability = 1000;
        private Supplier<AzItemRenderer> renderer;
        private AnimatorController animatorController = new GunAnimatorController();

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
            this.availableFireModes = Set.of(modes);
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
         * Sets the gun's durability
         * @param durability Maximum durability (how many shots before breaking)
         */
        public GunBuilder durability(int durability) {
            this.durability = durability;
            return this;
        }

        /**
         * Sets the gun's AzureLib item renderer.
         * @param renderer The AzureLib Item Renderer for this gun item.
         */
        public GunBuilder renderer(Supplier<AzItemRenderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        /**
         * Sets the gun's Gun Item renderer using the default item name.
         */
        public GunBuilder gunRenderer() {
            this.renderer = () -> new GunItemRenderer(this.name);
            return this;
        }

        /**
         * Sets a custom animator controller for this gun.
         * Use this to customize animation durations and add custom states.
         * @param controller The animator controller instance
         */
        public GunBuilder animatorController(AnimatorController controller) {
            this.animatorController = controller;
            return this;
        }

        /**
         * Registers the gun with all configured components
         */
        public DeferredItem<GunItem> register() {
            GunStatsComponent statsComponent = stats;
            GunMagazineComponent magazineComponent = new GunMagazineComponent(
                    startsLoaded ? magazineCapacity : 0,
                    magazineCapacity,
                    ammoType.getHolder()
            );
            GunFireModesComponent fireModesComponent = new GunFireModesComponent(
                    defaultFireMode,
                    List.copyOf(availableFireModes)
            );

            DeferredItem<GunItem> item = Roa.ITEMS.register(name, () -> new GunItem(
                    new Item.Properties()
                            .durability(durability),
                    statsComponent,
                    magazineComponent,
                    fireModesComponent,
                    animatorController
            ));

            RoaItemRenderers.RENDERERS.add(new RoaItemRenderers.Entry(renderer, item));

            return item;
        }
    }
}