package dev.ctrlneo.roa;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Roa.MODID)
public class RoaConfig {

    // ========== Client Config ==========
    public static class Client {
        // Accessibility
        public final ModConfigSpec.BooleanValue toggleAds;

        // Aiming
        public final ModConfigSpec.DoubleValue adsFovMultiplier;
        public final ModConfigSpec.DoubleValue adsSensitivityMultiplier;

        // Visual
        public final ModConfigSpec.BooleanValue showCrosshairWhileAds;
        public final ModConfigSpec.BooleanValue showAmmoCounter;
        public final ModConfigSpec.BooleanValue showFireModeIndicator;

        // Audio
        public final ModConfigSpec.DoubleValue gunSoundVolume;
        public final ModConfigSpec.DoubleValue reloadSoundVolume;

        Client(ModConfigSpec.Builder builder) {
            builder.push("accessibility");

            toggleAds = builder
                    .comment("If true, ADS is toggled on/off with right-click.",
                            "If false, you must hold right-click to ADS.")
                    .define("toggleAds", false);

            builder.pop();

            builder.push("aiming");

            adsFovMultiplier = builder
                    .comment("FOV multiplier when aiming down sights.",
                            "Lower values = more zoom. (0.1 = 10% of normal FOV, 1.0 = no zoom)")
                    .defineInRange("adsFovMultiplier", 0.7, 0.1, 1.0);

            adsSensitivityMultiplier = builder
                    .comment("Mouse sensitivity multiplier when aiming down sights.",
                            "Lower values = slower mouse movement while ADS.")
                    .defineInRange("adsSensitivityMultiplier", 0.5, 0.1, 1.0);

            builder.pop();

            builder.push("visual");

            showCrosshairWhileAds = builder
                    .comment("Whether to show the crosshair while aiming down sights.")
                    .define("showCrosshairWhileAds", false);

            showAmmoCounter = builder
                    .comment("Show ammo counter on the HUD.")
                    .define("showAmmoCounter", true);

            showFireModeIndicator = builder
                    .comment("Show current fire mode indicator on the HUD.")
                    .define("showFireModeIndicator", true);

            builder.pop();

            builder.push("audio");

            gunSoundVolume = builder
                    .comment("Volume multiplier for gun fire sounds.")
                    .defineInRange("gunSoundVolume", 1.0, 0.0, 2.0);

            reloadSoundVolume = builder
                    .comment("Volume multiplier for reload sounds.")
                    .defineInRange("reloadSoundVolume", 1.0, 0.0, 2.0);

            builder.pop();
        }
    }

    // ========== Server/Common Config ==========
    public static class Common {
        public final ModConfigSpec.BooleanValue allowGunDamage;
        public final ModConfigSpec.BooleanValue allowFriendlyFire;
        public final ModConfigSpec.DoubleValue globalDamageMultiplier;
        public final ModConfigSpec.IntValue maxAmmoStackSize;

        Common(ModConfigSpec.Builder builder) {
            builder.push("gameplay");

            allowGunDamage = builder
                    .comment("Whether guns can damage entities.",
                            "Set to false to disable all gun damage (useful for minigames).")
                    .define("allowGunDamage", true);

            allowFriendlyFire = builder
                    .comment("Whether guns can damage teammates/allies.")
                    .define("allowFriendlyFire", false);

            globalDamageMultiplier = builder
                    .comment("Global damage multiplier for all guns.",
                            "Use this to balance gun damage server-wide.")
                    .defineInRange("globalDamageMultiplier", 1.0, 0.0, 10.0);

            builder.pop();

            builder.push("items");

            maxAmmoStackSize = builder
                    .comment("Maximum stack size for ammo items.",
                            "Set to -1 to use default per-ammo-type limits.")
                    .defineInRange("maxAmmoStackSize", -1, -1, 999);

            builder.pop();
        }
    }

    // Static instances
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        // Build client config
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();

        // Build common config
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();
    }

    // Config reload handler
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        // Config values are automatically synced, no need for manual copying
        // But you can add validation or derived values here if needed
    }
}