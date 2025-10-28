package dev.ctrlneo.roa.foundation.animations.controller;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for animator controllers inspired by Unity's Mechanim.
 * Handles animation state machine logic with states, transitions, and conditions.
 * 
 * Extend this class to create custom animator controllers for different items.
 */
public abstract class AnimatorController {
    
    protected AnimationState currentState;
    protected long stateStartTick = 0;
    
    /**
     * Update the animator controller. Called every client tick.
     * Evaluates transitions and updates the current state.
     * 
     * @param player The local player
     * @param itemStack The item stack being animated
     * @return The animation that should be playing (null if no change)
     */
    public AnimationCommand update(LocalPlayer player, ItemStack itemStack) {
        if (currentState == null) {
            currentState = getDefaultState();
            stateStartTick = player.level().getGameTime();
            return currentState.getAnimationCommand();
        }
        
        long currentTick = player.level().getGameTime();
        
        // Check if current state has finished (for PLAY_ONCE animations)
        if (currentState.isPlayOnce() && hasStateFinished(currentTick)) {
            // Transition to next state
            AnimationState nextState = getStateAfterPlayOnce(player, itemStack);
            if (nextState != currentState) {
                return transitionTo(nextState, currentTick);
            }
        }
        
        // Evaluate transitions
        for (AnimationTransition transition : getTransitions()) {
            if (transition.from == currentState && transition.evaluateCondition(player, itemStack)) {
                return transitionTo(transition.to, currentTick);
            }
        }
        
        return null; // No state change
    }
    
    /**
     * Force a transition to a specific state.
     * Used for PLAY_ONCE animations like fire and reload.
     * 
     * @param state The state to transition to
     * @param player The local player
     * @return The animation command for the new state
     */
    public AnimationCommand forceState(AnimationState state, LocalPlayer player) {
        long currentTick = player.level().getGameTime();
        return transitionTo(state, currentTick);
    }
    
    /**
     * Transition to a new state.
     */
    private AnimationCommand transitionTo(AnimationState newState, long currentTick) {
        currentState = newState;
        stateStartTick = currentTick;
        return newState.getAnimationCommand();
    }
    
    /**
     * Check if the current state has finished playing.
     */
    private boolean hasStateFinished(long currentTick) {
        return (currentTick - stateStartTick) >= currentState.getDurationTicks();
    }
    
    /**
     * Reset the animator to default state.
     */
    public void reset() {
        currentState = getDefaultState();
        stateStartTick = 0;
    }
    
    /**
     * Get the current animation state.
     */
    public AnimationState getCurrentState() {
        return currentState;
    }
    
    /**
     * Get the default state (typically IDLE).
     * Override this to change the default state.
     */
    protected abstract AnimationState getDefaultState();
    
    /**
     * Get all possible transitions.
     * Override this to define state machine transitions.
     */
    protected abstract AnimationTransition[] getTransitions();
    
    /**
     * Determine which state to transition to after a PLAY_ONCE animation finishes.
     * Override this to customize behavior after one-shot animations.
     */
    protected abstract AnimationState getStateAfterPlayOnce(LocalPlayer player, ItemStack itemStack);
    
    /**
     * Represents an animation state in the state machine.
     */
    public static class AnimationState {
        private static final int TICKS_PER_SECOND = 20;
        
        private final String name;
        private final String animationName;
        private final boolean isPlayOnce;
        private final int durationTicks;
        
        /**
         * Create an animation state.
         * @param name Internal name for this state
         * @param animationName Animation name in AzureLib animation file
         * @param isPlayOnce Whether this is a one-shot animation
         * @param durationSeconds Duration in seconds (will be converted to ticks with proper rounding)
         */
        public AnimationState(String name, String animationName, boolean isPlayOnce, float durationSeconds) {
            this.name = name;
            this.animationName = animationName;
            this.isPlayOnce = isPlayOnce;
            // Use Math.round for proper rounding instead of truncation
            // Examples: 0.07s → 1.4 ticks → 1 tick
            //           1.14s → 22.8 ticks → 23 ticks
            this.durationTicks = Math.round(durationSeconds * TICKS_PER_SECOND);
        }
        
        public String getName() {
            return name;
        }
        
        public String getAnimationName() {
            return animationName;
        }
        
        public boolean isPlayOnce() {
            return isPlayOnce;
        }
        
        public int getDurationTicks() {
            return durationTicks;
        }
        
        public AnimationCommand getAnimationCommand() {
            return new AnimationCommand(animationName, isPlayOnce, durationTicks);
        }
    }
    
    /**
     * Represents a transition between two states with a condition.
     */
    public static class AnimationTransition {
        private final AnimationState from;
        private final AnimationState to;
        private final TransitionCondition condition;
        
        public AnimationTransition(AnimationState from, AnimationState to, TransitionCondition condition) {
            this.from = from;
            this.to = to;
            this.condition = condition;
        }
        
        public boolean evaluateCondition(LocalPlayer player, ItemStack itemStack) {
            return condition.evaluate(player, itemStack);
        }
    }
    
    /**
     * Functional interface for transition conditions.
     */
    @FunctionalInterface
    public interface TransitionCondition {
        boolean evaluate(LocalPlayer player, ItemStack itemStack);
    }
    
    /**
     * Animation command to be sent to the server/dispatcher.
     */
    public static class AnimationCommand {
        private final String animationName;
        private final boolean isPlayOnce;
        private final int durationTicks;
        
        public AnimationCommand(String animationName, boolean isPlayOnce, int durationTicks) {
            this.animationName = animationName;
            this.isPlayOnce = isPlayOnce;
            this.durationTicks = durationTicks;
        }
        
        public String getAnimationName() {
            return animationName;
        }
        
        public boolean isPlayOnce() {
            return isPlayOnce;
        }
        
        public int getDurationTicks() {
            return durationTicks;
        }
    }
}

