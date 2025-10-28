package dev.ctrlneo.roa.foundation.animations.controller.core;

import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;

/**
 * Represents an animation state in the state machine.
 */
public class AnimationState {
    private static final int TICKS_PER_SECOND = 20;

    private final String name;
    private final String animationName;
    private final AzPlayBehavior playBehavior;
    private final int durationTicks;

    /**
     * Create an animation state.
     *
     * @param name            Internal name for this state
     * @param animationName   Animation name in AzureLib animation file
     * @param playBehavior    AzureLib play behavior (LOOP, PLAY_ONCE, HOLD_ON_LAST_FRAME, etc.)
     * @param durationSeconds Duration in seconds (will be converted to ticks with proper rounding)
     */
    public AnimationState(String name, String animationName, AzPlayBehavior playBehavior, float durationSeconds) {
        this.name = name;
        this.animationName = animationName;
        this.playBehavior = playBehavior;
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

    public AzPlayBehavior getPlayBehavior() {
        return playBehavior;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    /**
     * Should this animation automatically return to a loop state after finishing?
     * True for PLAY_ONCE and HOLD_ON_LAST_FRAME.
     */
    public boolean shouldAutoReturn() {
        return playBehavior == AzPlayBehaviors.PLAY_ONCE || playBehavior == AzPlayBehaviors.HOLD_ON_LAST_FRAME;
    }

    public AnimationCommand getAnimationCommand() {
        return new AnimationCommand(animationName, playBehavior, durationTicks);
    }
}
