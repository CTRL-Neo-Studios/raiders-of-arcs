package dev.ctrlneo.roa.foundation.animations.controller;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.client.AdsStateManager;
import dev.ctrlneo.roa.foundation.data.components.GunStateComponent;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Base animator controller for guns with standard states:
 * IDLE, AIM, SPRINT, FIRE, AIM_FIRE, RELOAD
 * 
 * Extend this class to customize animation durations and add custom states.
 */
public class GunAnimatorController extends AnimatorController {
    
    // Standard gun states
    protected final AnimationState IDLE;
    protected final AnimationState AIM;
    protected final AnimationState SPRINT;
    protected final AnimationState FIRE;
    protected final AnimationState AIM_FIRE;
    protected final AnimationState RELOAD;
    
    // Transitions
    protected final AnimationTransition[] transitions;
    
    /**
     * Constructor with default animation durations.
     * Override getAnimationDurations() to customize per gun.
     */
    public GunAnimatorController() {
        AnimationDurations durations = getAnimationDurations();
        
        // Initialize states with durations (in seconds) and AzureLib play behaviors
        this.IDLE = new AnimationState("IDLE", "weapon.idle", AzPlayBehaviors.LOOP, 0);
        this.AIM = new AnimationState("AIM", "weapon.aim", AzPlayBehaviors.LOOP, 0);
        this.SPRINT = new AnimationState("SPRINT", "weapon.sprinting", AzPlayBehaviors.LOOP, 0);
        this.FIRE = new AnimationState("FIRE", "weapon.fire", AzPlayBehaviors.HOLD_ON_LAST_FRAME, durations.fireSeconds);
        this.AIM_FIRE = new AnimationState("AIM_FIRE", "weapon.aim_fire", AzPlayBehaviors.HOLD_ON_LAST_FRAME, durations.aimFireSeconds);
        this.RELOAD = new AnimationState("RELOAD", "weapon.reload", AzPlayBehaviors.HOLD_ON_LAST_FRAME, durations.reloadSeconds);
        
        // Define transitions (order matters - first match wins!)
        this.transitions = new AnimationTransition[] {
            // From any state to RELOAD when reloading
            new AnimationTransition(IDLE, RELOAD, this::isReloading),
            new AnimationTransition(AIM, RELOAD, this::isReloading),
            new AnimationTransition(SPRINT, RELOAD, this::isReloading),
            
            // From any non-reload state to SPRINT when sprinting
            new AnimationTransition(IDLE, SPRINT, this::isSprinting),
            new AnimationTransition(AIM, SPRINT, this::isSprinting),
            
            // From SPRINT to AIM when aiming (stops sprinting)
            new AnimationTransition(SPRINT, AIM, this::isAiming),
            
            // From SPRINT to IDLE when stopped sprinting
            new AnimationTransition(SPRINT, IDLE, (p, i) -> !isSprinting(p, i)),
            
            // Between IDLE and AIM
            new AnimationTransition(IDLE, AIM, this::isAiming),
            new AnimationTransition(AIM, IDLE, (p, i) -> !isAiming(p, i)),
        };
    }
    
    @Override
    protected AnimationState getDefaultState() {
        return IDLE;
    }
    
    @Override
    protected AnimationTransition[] getTransitions() {
        return transitions;
    }
    
    @Override
    protected AnimationState getStateAfterPlayOnce(LocalPlayer player, ItemStack itemStack) {
        // After PLAY_ONCE animations, return to appropriate loop state
        
        // Check priority: Reloading > Sprinting > Aiming > Idle
        if (isReloading(player, itemStack)) {
            return RELOAD;
        }
        
        if (isSprinting(player, itemStack)) {
            return SPRINT;
        }
        
        if (isAiming(player, itemStack)) {
            return AIM;
        }
        
        return IDLE;
    }
    
    /**
     * Get the FIRE state for triggering fire animation.
     */
    public AnimationState getFireState(boolean isAiming) {
        return isAiming ? AIM_FIRE : FIRE;
    }
    
    /**
     * Get the RELOAD state for triggering reload animation.
     */
    public AnimationState getReloadState() {
        return RELOAD;
    }
    
    // ========== CONDITION METHODS ==========
    
    protected boolean isAiming(LocalPlayer player, ItemStack itemStack) {
        return AdsStateManager.isPlayerAiming();
    }
    
    protected boolean isSprinting(LocalPlayer player, ItemStack itemStack) {
        // Only sprint animation if sprinting AND on ground
        return player.isSprinting() && player.onGround();
    }
    
    protected boolean isReloading(LocalPlayer player, ItemStack itemStack) {
        GunStateComponent state = itemStack.get(RoaDataComponents.GUN_STATE.get());
        return state != null && state.isReloading();
    }
    
    // ========== CUSTOMIZATION ==========
    
    /**
     * Override this method to customize animation durations for specific guns.
     * This is where you define how long each PLAY_ONCE animation lasts.
     * All durations are in SECONDS.
     */
    protected AnimationDurations getAnimationDurations() {
        return new AnimationDurations(
            0.5f,  // fire: 0.5 seconds
            0.5f,  // aimFire: 0.5 seconds
            5.0f   // reload: 5 seconds
        );
    }
    
    /**
     * Data class to hold animation durations in SECONDS.
     */
    protected static class AnimationDurations {
        public final float fireSeconds;
        public final float aimFireSeconds;
        public final float reloadSeconds;
        
        /**
         * Create animation durations.
         * @param fireSeconds Duration of fire animation in seconds
         * @param aimFireSeconds Duration of aim-fire animation in seconds
         * @param reloadSeconds Duration of reload animation in seconds
         */
        public AnimationDurations(float fireSeconds, float aimFireSeconds, float reloadSeconds) {
            this.fireSeconds = fireSeconds;
            this.aimFireSeconds = aimFireSeconds;
            this.reloadSeconds = reloadSeconds;
        }
    }
}

