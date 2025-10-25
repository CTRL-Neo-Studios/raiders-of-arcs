package dev.ctrlneo.roa.foundation.client;

import net.minecraft.util.Mth;

public class AdsStateManager {
    private static boolean isAiming = false;
    private static boolean wasAdsPressed = false;

    // Current and previous progress for smooth interpolation
    private static float adsProgress = 0.0f;
    private static float prevAdsProgress = 0.0f;
    private static float targetAdsProgress = 0.0f;

    private static float transitionSpeed = 0.2f;

    public static boolean isPlayerAiming() {
        return isAiming;
    }

    public static void setAiming(boolean aiming) {
        isAiming = aiming;
        targetAdsProgress = aiming ? 1.0f : 0.0f;
    }

    public static boolean wasAdsPressed() {
        return wasAdsPressed;
    }

    public static void setWasAdsPressed(boolean pressed) {
        wasAdsPressed = pressed;
    }

    /**
     * Get current ADS progress WITH partial tick interpolation for smooth rendering
     * @param partialTicks Frame interpolation value (0.0 to 1.0)
     * @return smoothly interpolated value between 0.0 and 1.0
     */
    public static float getAdsProgress(float partialTicks) {
        return Mth.lerp(partialTicks, prevAdsProgress, adsProgress);
    }

    /**
     * Get current ADS progress without interpolation (for logic checks)
     */
    public static float getAdsProgress() {
        return adsProgress;
    }

    /**
     * Update ADS progress - called once per tick
     */
    public static void updateAdsProgress() {
        // Store previous value for interpolation
        prevAdsProgress = adsProgress;

        if (Math.abs(adsProgress - targetAdsProgress) > 0.001f) {
            // Move towards target
            float delta = targetAdsProgress - adsProgress;
            adsProgress = Mth.clamp(adsProgress + (delta * transitionSpeed), 0.0f, 1.0f);
        } else {
            // Snap to target when very close
            adsProgress = targetAdsProgress;
        }
    }

    /**
     * Set transition speed based on gun's ADS speed stat
     * @param adsSpeedInSeconds Time to fully ADS in seconds
     */
    public static void setTransitionSpeed(float adsSpeedInSeconds) {
        // Convert seconds to per-tick speed (20 ticks per second)
        // We want to reach the target in adsSpeedInSeconds
        // Speed per tick = 1.0 / (adsSpeedInSeconds * 20 ticks/second)
        transitionSpeed = Mth.clamp(1.0f / (adsSpeedInSeconds * 20.0f), 0.05f, 1.0f);
    }

    public static boolean isTransitioning() {
        return adsProgress > 0.0f && adsProgress < 1.0f;
    }

    public static void reset() {
        isAiming = false;
        wasAdsPressed = false;
        adsProgress = 0.0f;
        prevAdsProgress = 0.0f;
        targetAdsProgress = 0.0f;
        transitionSpeed = 0.2f;
    }
}