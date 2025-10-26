package dev.ctrlneo.roa.foundation;

import dev.ctrlneo.roa.Roa;
import dev.ctrlneo.roa.foundation.data.structures.AmmoType;
import dev.ctrlneo.roa.foundation.data.structures.GunFireMode;
import dev.ctrlneo.roa.foundation.items.GunItem;
import dev.ctrlneo.roa.foundation.utils.GunRegistryHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
            .stats(3f, 0.8f, 1.5f, 1f, 350, 30, 0.2f, 0.1f, 0.1f, 5.0f)
            .register();

    public static final DeferredItem<GunItem> RATTLER = GunRegistryHelper.gun("rattler")
            .ammo(AmmoType.MEDIUM, 20)
            .fireModes(GunFireMode.AUTOMATIC_FIRE, GunFireMode.SINGLE_FIRE)
            .startsEmpty()
            .gunRenderer()
            .stats(2f, 0.85f, 2.2f, 2f, 400, 56, 0.6f, 0.15f, 0.1f, 3.5f)
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
