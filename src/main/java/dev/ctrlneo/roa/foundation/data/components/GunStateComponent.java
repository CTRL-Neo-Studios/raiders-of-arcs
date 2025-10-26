package dev.ctrlneo.roa.foundation.data.components;

public record GunStateComponent(
        boolean isReloading,
        long reloadStartTime,  // NEW: When reload started
        long lastFireTime,
        int burstShotsFired
) {
    public static final GunStateComponent DEFAULT = new GunStateComponent(false, 0, 0, 0);

    public GunStateComponent withReloading(boolean reloading, long startTime) {
        return new GunStateComponent(reloading, startTime, lastFireTime, burstShotsFired);
    }

    public GunStateComponent cancelReload() {
        return new GunStateComponent(false, 0, lastFireTime, burstShotsFired);
    }

    public GunStateComponent completeReload() {
        return new GunStateComponent(false, 0, lastFireTime, burstShotsFired);
    }

    public GunStateComponent fired(long currentTime) {
        return new GunStateComponent(false, 0, currentTime, burstShotsFired + 1);
    }

    public GunStateComponent resetBurst() {
        return new GunStateComponent(isReloading, reloadStartTime, lastFireTime, 0);
    }

    public boolean canFire(long currentTime, long minTicksBetweenShots) {
        if (isReloading) return false;
        return (currentTime - lastFireTime) >= minTicksBetweenShots;
    }

    /**
     * Get reload progress as a value from 0.0 to 1.0
     */
    public float getReloadProgress(long currentTime, int reloadDurationTicks) {
        if (!isReloading || reloadStartTime == 0) {
            return 0.0f;
        }
        long elapsed = currentTime - reloadStartTime;
        return Math.min(1.0f, (float) elapsed / reloadDurationTicks);
    }

    /**
     * Check if reload is complete
     */
    public boolean isReloadComplete(long currentTime, int reloadDurationTicks) {
        return isReloading && (currentTime - reloadStartTime) >= reloadDurationTicks;
    }
}