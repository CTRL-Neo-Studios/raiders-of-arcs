package dev.ctrlneo.roa.foundation.animations.controller.guns;

import dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController;

/**
 * Custom animator controller for the Kettle gun.
 * Demonstrates how to customize animation durations for a specific gun.
 * 
 * You can also add custom states and transitions here if needed.
 */
public class KettleGunAnimatorController extends GunAnimatorController {
    
    @Override
    protected AnimationDurations getAnimationDurations() {
        return new AnimationDurations(
            0.07f,
            0.07f,
            5f
        );
    }
}

