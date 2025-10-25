package dev.ctrlneo.roa.foundation.data.components;

public record GunStatsComponent(
        float damage,
        float accuracy,
        float recoilVertical,
        float recoilHorizontal,
        int fireRate,
        float range,
        float armorPenetration,
        float adsSpeed,         // Time in seconds to aim down sights
        float unholsterSpeed,   // Time in seconds to ready the weapon
        float reloadSpeed       // Time in seconds to reload
) {
    public static final GunStatsComponent DEFAULT = new GunStatsComponent(
            10.0f,   // damage
            0.8f,    // accuracy
            1.0f,    // vertical recoil
            1.0f,    // horizontal recoil
            600,     // fire rate (RPM)
            50.0f,   // range
            0.0f,    // armor penetration
            0.3f,    // ADS speed
            0.5f,    // unholster speed
            2.0f     // reload speed
    );

    public long getTicksBetweenShots() {
        return 1200L / fireRate;
    }

    public int getReloadTicks() {
        return (int)(reloadSpeed * 20); // Convert seconds to ticks
    }

    public int getAdsTicks() {
        return (int)(adsSpeed * 20);
    }

    public int getUnholsterTicks() {
        return (int)(unholsterSpeed * 20);
    }

    /**
     * Apply attachment modifiers to create a new stats component
     */
    public GunStatsComponent withAttachmentModifiers(AttachmentModifiersComponent modifiers) {
        return new GunStatsComponent(
                damage + modifiers.damageBonus(),
                Math.min(1.0f, accuracy * modifiers.accuracyMultiplier()),
                recoilVertical * modifiers.recoilMultiplier(),
                recoilHorizontal * modifiers.recoilMultiplier(),
                fireRate,
                range + modifiers.rangeBonus(),
                Math.min(1.0f, armorPenetration + modifiers.armorPenetrationBonus()),
                adsSpeed * modifiers.adsSpeedMultiplier(),
                unholsterSpeed,
                reloadSpeed * modifiers.reloadSpeedMultiplier()
        );
    }
}