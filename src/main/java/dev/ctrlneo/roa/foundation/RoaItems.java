package dev.ctrlneo.roa.foundation;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.animations.controller.guns.KettleGunAnimatorController;
import dev.ctrlneo.roa.foundation.animations.controller.guns.RattlerGunAnimatorController;
import dev.ctrlneo.roa.foundation.data.components.GunAttributeModifier;
import dev.ctrlneo.roa.foundation.data.structures.AmmoType;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.data.structures.GunLevelConfig;
import dev.ctrlneo.roa.foundation.data.structures.LevelModifiers;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.utils.GunRegistryHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class RoaItems {
    public static final DeferredItem<Item> HEAVY_AMMO = Roa.ITEMS.registerSimpleItem("heavy_ammo", new Item.Properties().stacksTo(40).setNoRepair());
    public static final DeferredItem<Item> MEDIUM_AMMO = Roa.ITEMS.registerSimpleItem("medium_ammo", new Item.Properties().stacksTo(80).setNoRepair());
    public static final DeferredItem<Item> LIGHT_AMMO = Roa.ITEMS.registerSimpleItem("light_ammo", new Item.Properties().stacksTo(100).setNoRepair());
    public static final DeferredItem<Item> SHOTGUN_AMMO = Roa.ITEMS.registerSimpleItem("shotgun_ammo", new Item.Properties().stacksTo(20).setNoRepair());
    public static final DeferredItem<Item> LAUNCHER_AMMO = Roa.ITEMS.registerSimpleItem("launcher_ammo", new Item.Properties().stacksTo(24).setNoRepair());
    public static final DeferredItem<Item> ENERGY_CLIP = Roa.ITEMS.registerSimpleItem("energy_clip", new Item.Properties().stacksTo(5).setNoRepair());

    public static final DeferredItem<GunItem> KETTLE = GunRegistryHelper.gun("kettle")
            .ammo(AmmoType.LIGHT, 20)
            .fireModes(GunFireMode.SINGLE_FIRE)
            .startsEmpty()
            .gunRenderer()
            .stats(3f, 2f, 1.8f, 0.5f, 350, 42.8f, 0.2f, 0.2f, 0.1f, 0.8f)  // Removed reloadSpeed - now in .reloadOneShot()
            .animatorController(new KettleGunAnimatorController())
            .reloadOneShot(6.0f)
            .levelConfig(GunLevelConfig.builder()
                    .maxLevel(4)
                    .level(2, LevelModifiers.builder()
                            .multiplyBase(GunAttributeModifier.GunAttribute.DAMAGE, 1.1f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RANGE, 1.25f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RELOAD_SPEED, 0.9f)
                            .build())
                    .level(3, LevelModifiers.builder()
                            .multiplyBase(GunAttributeModifier.GunAttribute.DAMAGE, 1.1f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RANGE, 1.5f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RELOAD_SPEED, 0.95f)
                            .build())
                    .level(4, LevelModifiers.builder()
                            .multiplyBase(GunAttributeModifier.GunAttribute.DAMAGE, 1.1f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RANGE, 1.75f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RELOAD_SPEED, 0.95f)
                            .build())
                    .build())
            .register();

    public static final DeferredItem<GunItem> RATTLER = GunRegistryHelper.gun("rattler")
            .ammo(AmmoType.MEDIUM, 10)
            .fireModes(GunFireMode.AUTOMATIC_FIRE, GunFireMode.SINGLE_FIRE)
            .startsEmpty()
            .gunRenderer()
            .stats(2f, 1.6f, 2f, 0.9f, 400, 56, 0.6f, 0.15f, 0.1f, 0.8f)  // Removed reloadSpeed - now in .reloadSequential()
            .animatorController(new RattlerGunAnimatorController())
            .reloadSequential(2, 0.5f)
            .levelConfig(GunLevelConfig.builder()
                    .maxLevel(4)
                    .level(2, LevelModifiers.builder()
                            .add(GunAttributeModifier.GunAttribute.MAGAZINE_CAPACITY, 4)
                            .multiplyBase(GunAttributeModifier.GunAttribute.ADS_SPEED, 1.15f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RANGE, 1.1f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RECOIL_HORIZONTAL, 1.2f)
                            .build())
                    .level(3, LevelModifiers.builder()
                            .add(GunAttributeModifier.GunAttribute.MAGAZINE_CAPACITY, 4)
                            .multiplyBase(GunAttributeModifier.GunAttribute.ADS_SPEED, 1.15f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RANGE, 1.1f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RECOIL_VERTICAL, 1.5f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RECOIL_HORIZONTAL, 1.2f)
                            .build())
                    .level(4, LevelModifiers.builder()
                            .add(GunAttributeModifier.GunAttribute.MAGAZINE_CAPACITY, 4)
                            .multiplyBase(GunAttributeModifier.GunAttribute.ADS_SPEED, 1.1f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RANGE, 1.2f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RECOIL_VERTICAL, 1.1f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.RECOIL_HORIZONTAL, 1.5f)
                            .multiplyBase(GunAttributeModifier.GunAttribute.ACCURACY, 1.1f)
                            .add(GunAttributeModifier.GunAttribute.DAMAGE, 1)
                            .build())
                    .build())
            .register();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ROA_TAB =
            Roa.CREATIVE_MODE_TABS.register("roa_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.roa"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> RoaItems.HEAVY_AMMO.get().getDefaultInstance()) // This is fine now
                    .displayItems((parameters, output) -> {
                        output.accept(RoaItems.LIGHT_AMMO);
                        output.accept(RoaItems.MEDIUM_AMMO);
                        output.accept(RoaItems.HEAVY_AMMO);
                        output.accept(RoaItems.SHOTGUN_AMMO);
                        output.accept(RoaItems.LAUNCHER_AMMO);
                        output.accept(RoaItems.ENERGY_CLIP);

                        // Add guns with default components (loaded or empty as configured)
                        output.accept(RoaItems.KETTLE.get().getDefaultInstance());
                        output.accept(RoaItems.RATTLER.get().getDefaultInstance());
                    }).build());

    public static void register() {

    }
}
