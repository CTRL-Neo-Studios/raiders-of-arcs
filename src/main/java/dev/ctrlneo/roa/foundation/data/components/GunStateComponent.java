package dev.ctrlneo.roa.foundation.data.components;

/**
 * Tracks the dynamic state of a gun during gameplay.
 * This includes transient states like reloading, unholstering, and firing.
 */
public record GunStateComponent(
        boolean isReloading,
        long reloadStartTime,         // When current reload sequence started
        int currentSequenceRounds,    // For SEQUENTIAL: how many rounds this sequence will add
        boolean isUnholstering,
        long unholsterStartTime,      // When unholster animation started
        long lastFireTime,            // Last time the gun was fired
        int burstShotsFired           // Track burst fire count
) {
    public static final GunStateComponent DEFAULT = new GunStateComponent(
            false, 0, 0,  // Reload state
            false, 0,     // Unholster state
            0, 0          // Fire state
    );

    // ========== RELOAD STATE METHODS ==========
    
    public GunStateComponent withReloading(boolean reloading, long startTime, int sequenceRounds) {
        return new GunStateComponent(
                reloading, startTime, sequenceRounds,
                isUnholstering, unholsterStartTime,
                lastFireTime, burstShotsFired
        );
    }

    public GunStateComponent cancelReload() {
        return new GunStateComponent(
                false, 0, 0,
                isUnholstering, unholsterStartTime,
                lastFireTime, burstShotsFired
        );
    }

    public GunStateComponent completeReload() {
        return new GunStateComponent(
                false, 0, 0,
                isUnholstering, unholsterStartTime,
                lastFireTime, burstShotsFired
        );
    }
    
    /**
     * Start next reload sequence (for SEQUENTIAL reloads).
     */
    public GunStateComponent startNextSequence(long currentTime, int sequenceRounds) {
        return new GunStateComponent(
                true, currentTime, sequenceRounds,
                isUnholstering, unholsterStartTime,
                lastFireTime, burstShotsFired
        );
    }

    public boolean isReloadComplete(long currentTime, int reloadDurationTicks) {
        return isReloading && (currentTime - reloadStartTime) >= reloadDurationTicks;
    }

    public float getReloadProgress(long currentTime, int reloadDurationTicks) {
        if (!isReloading || reloadStartTime == 0) {
            return 0.0f;
        }
        long elapsed = currentTime - reloadStartTime;
        return Math.min(1.0f, (float) elapsed / reloadDurationTicks);
    }

    // ========== UNHOLSTER STATE METHODS ==========
    
    public GunStateComponent withUnholstering(boolean unholstering, long startTime) {
        return new GunStateComponent(
                isReloading, reloadStartTime, currentSequenceRounds,
                unholstering, startTime,
                lastFireTime, burstShotsFired
        );
    }

    public GunStateComponent completeUnholster() {
        return new GunStateComponent(
                isReloading, reloadStartTime, currentSequenceRounds,
                false, 0,
                lastFireTime, burstShotsFired
        );
    }

    public boolean isUnholsterComplete(long currentTime, int unholsterDurationTicks) {
        return isUnholstering && (currentTime - unholsterStartTime) >= unholsterDurationTicks;
    }

    // ========== FIRE STATE METHODS ==========
    
    public GunStateComponent fired(long currentTime) {
        return new GunStateComponent(
                false, 0, 0,
                isUnholstering, unholsterStartTime,
                currentTime, burstShotsFired + 1
        );
    }

    public GunStateComponent resetBurst() {
        return new GunStateComponent(
                isReloading, reloadStartTime, currentSequenceRounds,
                isUnholstering, unholsterStartTime,
                lastFireTime, 0
        );
    }

    public boolean canFire(long currentTime, long minTicksBetweenShots) {
        if (isReloading || isUnholstering) return false;
        return (currentTime - lastFireTime) >= minTicksBetweenShots;
    }
}