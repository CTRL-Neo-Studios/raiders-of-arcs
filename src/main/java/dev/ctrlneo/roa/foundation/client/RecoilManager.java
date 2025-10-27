package dev.ctrlneo.roa.foundation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class RecoilManager {

    // Current and target recoil values (in degrees)
    private static float currentRecoilPitch = 0.0f;
    private static float currentRecoilYaw = 0.0f;
    private static float targetRecoilPitch = 0.0f;
    private static float targetRecoilYaw = 0.0f;
    
    // Previous tick values for interpolation
    private static float prevRecoilPitch = 0.0f;
    private static float prevRecoilYaw = 0.0f;

    // Recovery tracking - snapshot of recoil when recovery starts
    private static float recoveryStartPitch = 0.0f;
    private static float recoveryStartYaw = 0.0f;
    private static boolean isRecovering = false;

    // Recoil timing - only recovery is smooth, application is instant
    private static final float RECOIL_RECOVERY_DURATION = 12.0f; // Ticks to recover smoothly
    
    private static float recoveryProgress = 0.0f;

    /**
     * Add recoil to be applied (called when gun fires)
     * Values are in "recoil units" and will be scaled by FOV
     */
    public static void addRecoil(float pitchRecoil, float yawRecoil) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        // Scale recoil by FOV to be consistent across resolutions
        // This makes recoil feel the same regardless of screen size
        double fovMultiplier = 70.0 / mc.options.fov().get(); // Normalize to 70 FOV
        
        // Reduce recoil when aiming down sights
        float adsMultiplier = 1.0f;
        if (AdsStateManager.isPlayerAiming()) {
            adsMultiplier = 0.5f; // 50% less recoil when ADS (more noticeable)
        }

        // Apply FOV and ADS scaling
        float scaledPitch = pitchRecoil * (float) fovMultiplier * adsMultiplier;
        float scaledYaw = yawRecoil * (float) fovMultiplier * adsMultiplier;

        // Accumulate recoil (pitch is NEGATIVE for upward in Minecraft!)
        // For automatic fire, this adds to existing recoil
        targetRecoilPitch -= scaledPitch;
        targetRecoilYaw += scaledYaw;
        
        // Apply recoil INSTANTLY for punchy feel
        currentRecoilPitch = targetRecoilPitch;
        currentRecoilYaw = targetRecoilYaw;
        
        // Cancel recovery - we're firing again!
        isRecovering = false;
        recoveryProgress = 0.0f;
    }

    /**
     * Update recoil state (called every client tick)
     */
    public static void updateRecoil() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            reset();
            return;
        }

        // Store previous tick values for interpolation
        prevRecoilPitch = currentRecoilPitch;
        prevRecoilYaw = currentRecoilYaw;

        // Recovery phase: smoothly return to neutral
        // Note: Application is instant (happens in addRecoil), only recovery is smooth
        if (Math.abs(targetRecoilPitch) > 0.01f || Math.abs(targetRecoilYaw) > 0.01f) {
            
            // Start recovery if not already recovering
            if (!isRecovering) {
                isRecovering = true;
                recoveryProgress = 0.0f;
                recoveryStartPitch = targetRecoilPitch;
                recoveryStartYaw = targetRecoilYaw;
            }
            
            // Increment recovery progress
            recoveryProgress = Math.min(1.0f, recoveryProgress + (1.0f / RECOIL_RECOVERY_DURATION));
            
            // Use smootherstep for smooth recovery
            float smoothRecovery = smootherstep(recoveryProgress);
            
            // Lerp from recovery start position to 0
            targetRecoilPitch = Mth.lerp(smoothRecovery, recoveryStartPitch, 0.0f);
            targetRecoilYaw = Mth.lerp(smoothRecovery, recoveryStartYaw, 0.0f);
            
            // Current follows target during recovery
            currentRecoilPitch = targetRecoilPitch;
            currentRecoilYaw = targetRecoilYaw;

            // Snap to 0 if very close
            if (Math.abs(targetRecoilPitch) < 0.01f) {
                targetRecoilPitch = 0.0f;
                currentRecoilPitch = 0.0f;
            }
            if (Math.abs(targetRecoilYaw) < 0.01f) {
                targetRecoilYaw = 0.0f;
                currentRecoilYaw = 0.0f;
            }
            
            // Check if recovery is complete
            if (recoveryProgress >= 1.0f) {
                isRecovering = false;
                recoveryProgress = 0.0f;
            }
        }
    }
    
    /**
     * Get interpolated recoil pitch for rendering (with partial ticks)
     * This is what makes it smooth like the FOV!
     */
    public static float getInterpolatedPitch(float partialTicks) {
        return Mth.lerp(partialTicks, prevRecoilPitch, currentRecoilPitch);
    }
    
    /**
     * Get interpolated recoil yaw for rendering (with partial ticks)
     */
    public static float getInterpolatedYaw(float partialTicks) {
        return Mth.lerp(partialTicks, prevRecoilYaw, currentRecoilYaw);
    }
    
    /**
     * Smootherstep function for smooth recovery
     * Ken Perlin's smootherstep: 6t⁵ - 15t⁴ + 10t³
     */
    private static float smootherstep(float t) {
        return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
    }

    public static void reset() {
        currentRecoilPitch = 0.0f;
        currentRecoilYaw = 0.0f;
        targetRecoilPitch = 0.0f;
        targetRecoilYaw = 0.0f;
        prevRecoilPitch = 0.0f;
        prevRecoilYaw = 0.0f;
        recoveryStartPitch = 0.0f;
        recoveryStartYaw = 0.0f;
        isRecovering = false;
        recoveryProgress = 0.0f;
    }

    public static float getCurrentRecoilPitch() {
        return currentRecoilPitch;
    }

    public static float getCurrentRecoilYaw() {
        return currentRecoilYaw;
    }
}