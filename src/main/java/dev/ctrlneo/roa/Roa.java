package dev.ctrlneo.roa;

import com.mojang.logging.LogUtils;
import dev.ctrlneo.roa.foundation.*;
import dev.ctrlneo.roa.foundation.data.codecs.RoaDataCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Roa.MODID)
public class Roa {
    public static final String MODID = "roa";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Roa.MODID);

    public Roa(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        RoaItems.register();
        RoaDataComponents.register();
        RoaKeybinds.register();
        RoaDataCodecs.register();
        RoaEntityTypes.register();
        RoaEntities.register();


        // IMPORTANT: Register data components FIRST, before items
        DATA_COMPONENTS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        // Register blocks, items, and creative tabs
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events
        NeoForge.EVENT_BUS.register(this);

        // Register packets
        modEventBus.addListener(RoaPackets::register);

        // Register configs
        modContainer.registerConfig(ModConfig.Type.CLIENT, RoaConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, RoaConfig.COMMON_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID)
    public static class ModEvents {
        // Optional: Add items to vanilla creative tabs
        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            // Add guns to combat tab
//            if (event.getTabKey() == CreativeModeTabs.COMBAT) {
//                event.accept(RoaItems.KETTLE);
//                event.accept(RoaItems.RATTLER);
//            }
//
//            // Add ammo to ingredients tab
//            if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
//                event.accept(RoaItems.LIGHT_AMMO);
//                event.accept(RoaItems.MEDIUM_AMMO);
//                event.accept(RoaItems.HEAVY_AMMO);
//                event.accept(RoaItems.SHOTGUN_AMMO);
//                event.accept(RoaItems.LAUNCHER_AMMO);
//                event.accept(RoaItems.ENERGY_CLIP);
//            }
        }
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            RoaItemRenderers.register();
        }
    }
}