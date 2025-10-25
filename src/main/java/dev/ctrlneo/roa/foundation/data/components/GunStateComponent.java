package dev.ctrlneo.roa.foundation.data.components;

public record GunStateComponent(
        boolean isReloading,
        long lastFireTime,
        int burstShotsFired
) {
    public static final GunStateComponent DEFAULT = new GunStateComponent(false, 0, 0);

    public GunStateComponent withReloading(boolean reloading) {
        return new GunStateComponent(reloading, lastFireTime, burstShotsFired);
    }

    public GunStateComponent fired(long currentTime) {
        return new GunStateComponent(false, currentTime, burstShotsFired + 1);
    }

    public GunStateComponent resetBurst() {
        return new GunStateComponent(isReloading, lastFireTime, 0);
    }

    public boolean canFire(long currentTime, long minTicksBetweenShots) {
        if (isReloading) return false;
        return (currentTime - lastFireTime) >= minTicksBetweenShots;
    }
}