package dev.ctrlneo.roa.foundation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class RecoilManager {

    private static float currentRecoilPitch = 0.0f;
    private static float currentRecoilYaw = 0.0f;
    private static float targetRecoilPitch = 0.0f;
    private static float targetRecoilYaw = 0.0f;

    // How fast recoil is applied and recovered
    private static final float RECOIL_APPLY_SPEED = 0.8f;
    private static final float RECOIL_RECOVERY_SPEED = 0.15f;

    /**
     * Add recoil to be applied
     * Positive pitch = upward recoil, Positive yaw = right recoil
     */
    public static void addRecoil(float pitchRecoil, float yawRecoil) {
        // Reduce recoil when aiming down sights
        float adsMultiplier = 1.0f;
        if (AdsStateManager.isPlayerAiming()) {
            adsMultiplier = 0.8f; // 20% less recoil when ADS
        }

        // Accumulate recoil (pitch is NEGATIVE for upward in Minecraft!)
        targetRecoilPitch -= pitchRecoil * adsMultiplier;  // Changed to subtract!
        targetRecoilYaw += yawRecoil * adsMultiplier;
    }

    /**
     * Update and apply recoil to player camera
     */
    public static void updateRecoil() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            reset();
            return;
        }

        // Store previous values
        float prevPitch = currentRecoilPitch;
        float prevYaw = currentRecoilYaw;

        // Smoothly apply recoil
        if (Math.abs(currentRecoilPitch - targetRecoilPitch) > 0.01f ||
                Math.abs(currentRecoilYaw - targetRecoilYaw) > 0.01f) {

            currentRecoilPitch = Mth.lerp(RECOIL_APPLY_SPEED, currentRecoilPitch, targetRecoilPitch);
            currentRecoilYaw = Mth.lerp(RECOIL_APPLY_SPEED, currentRecoilYaw, targetRecoilYaw);

            // Apply delta to player rotation
            float deltaPitch = currentRecoilPitch - prevPitch;
            float deltaYaw = currentRecoilYaw - prevYaw;

            player.setXRot(player.getXRot() + deltaPitch);
            player.setYRot(player.getYRot() + deltaYaw);
        }

        // Recover from recoil (move back towards 0)
        if (Math.abs(targetRecoilPitch) > 0.01f || Math.abs(targetRecoilYaw) > 0.01f) {
            targetRecoilPitch = Mth.lerp(RECOIL_RECOVERY_SPEED, targetRecoilPitch, 0.0f);
            targetRecoilYaw = Mth.lerp(RECOIL_RECOVERY_SPEED, targetRecoilYaw, 0.0f);

            // Snap to 0 if very close
            if (Math.abs(targetRecoilPitch) < 0.01f) targetRecoilPitch = 0.0f;
            if (Math.abs(targetRecoilYaw) < 0.01f) targetRecoilYaw = 0.0f;
        }

        // Reset if fully recovered
        if (targetRecoilPitch == 0.0f && targetRecoilYaw == 0.0f &&
                Math.abs(currentRecoilPitch) < 0.01f && Math.abs(currentRecoilYaw) < 0.01f) {
            currentRecoilPitch = 0.0f;
            currentRecoilYaw = 0.0f;
        }
    }

    private static float previousPitch = 0.0f;
    private static float previousYaw = 0.0f;

    private static void storePrevious(float pitch, float yaw) {
        previousPitch = pitch;
        previousYaw = yaw;
    }

    private static float getPreviousPitch() {
        return previousPitch;
    }

    private static float getPreviousYaw() {
        return previousYaw;
    }

    public static void reset() {
        currentRecoilPitch = 0.0f;
        currentRecoilYaw = 0.0f;
        targetRecoilPitch = 0.0f;
        targetRecoilYaw = 0.0f;
        previousPitch = 0.0f;
        previousYaw = 0.0f;
    }

    public static float getCurrentRecoilPitch() {
        return currentRecoilPitch;
    }

    public static float getCurrentRecoilYaw() {
        return currentRecoilYaw;
    }
}