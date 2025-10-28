package dev.ctrlneo.roa.foundation.animations.controller;

import dev.ctrlneo.roa.foundation.RoaDataComponents;
import dev.ctrlneo.roa.foundation.animations.controller.core.AnimationState;
import dev.ctrlneo.roa.foundation.animations.controller.core.AnimationTransition;
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
    
    // Transition animations (in/out)
    protected final AnimationState AIM_IN;
    protected final AnimationState AIM_OUT;
    protected final AnimationState SPRINT_IN;
    protected final AnimationState SPRINT_OUT;
    
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
        
        // Transition animations (all 0.2 seconds, HOLD_ON_LAST_FRAME)
        this.AIM_IN = new AnimationState("AIM_IN", "weapon.aim.in", AzPlayBehaviors.HOLD_ON_LAST_FRAME, 0.2f);
        this.AIM_OUT = new AnimationState("AIM_OUT", "weapon.aim.out", AzPlayBehaviors.HOLD_ON_LAST_FRAME, 0.2f);
        this.SPRINT_IN = new AnimationState("SPRINT_IN", "weapon.sprinting.in", AzPlayBehaviors.HOLD_ON_LAST_FRAME, 0.2f);
        this.SPRINT_OUT = new AnimationState("SPRINT_OUT", "weapon.sprinting.out", AzPlayBehaviors.HOLD_ON_LAST_FRAME, 0.2f);
        
        // Define transitions (order matters - first match wins!)
        this.transitions = new AnimationTransition[] {
            // From any state to RELOAD when reloading (no transition anim for reload)
            new AnimationTransition(IDLE, RELOAD, this::isReloading),
            new AnimationTransition(AIM, RELOAD, this::isReloading),
            new AnimationTransition(SPRINT, RELOAD, this::isReloading),
            
            // Transition animations complete - go to final state
            new AnimationTransition(AIM_IN, AIM, (p, i) -> true), // Always transition after AIM_IN finishes
            new AnimationTransition(AIM_OUT, IDLE, (p, i) -> true), // Always transition after AIM_OUT finishes
            new AnimationTransition(SPRINT_IN, SPRINT, (p, i) -> true), // Always transition after SPRINT_IN finishes
            new AnimationTransition(SPRINT_OUT, IDLE, (p, i) -> true), // Always transition after SPRINT_OUT finishes
            
            // From any non-reload state to SPRINT (via transition animation)
            new AnimationTransition(IDLE, SPRINT_IN, this::isSprinting),
            new AnimationTransition(AIM, SPRINT, this::isSprinting), // Skip transition from AIM (too complex)
            
            // From SPRINT to AIM when aiming (stops sprinting - skip transition for simplicity)
            new AnimationTransition(SPRINT, AIM, this::isAiming),
            
            // From SPRINT to IDLE when stopped sprinting (via transition animation)
            new AnimationTransition(SPRINT, SPRINT_OUT, (p, i) -> !isSprinting(p, i)),
            
            // Between IDLE and AIM (via transition animations)
            new AnimationTransition(IDLE, AIM_IN, this::isAiming),
            new AnimationTransition(AIM, AIM_OUT, (p, i) -> !isAiming(p, i)),
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
        // After HOLD_ON_LAST_FRAME animations, determine next state
        
        // Transition animations always go to their destination state
        if (currentState == AIM_IN) return AIM;
        if (currentState == AIM_OUT) return IDLE;
        if (currentState == SPRINT_IN) return SPRINT;
        if (currentState == SPRINT_OUT) return IDLE;
        
        // After action animations (FIRE, RELOAD), return to appropriate loop state
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

