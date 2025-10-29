package dev.ctrlneo.roa.foundation.animations.controller.guns;

import dev.ctrlneo.roa.foundation.animations.controller.GunAnimatorController;

/**
 * Animator controller for the Rattler gun (automatic rifle).
 * Fast fire rate with quick animations.
 */
public class RattlerGunAnimatorController extends GunAnimatorController {
    
    @Override
    protected AnimationDurations getAnimationDurations() {
        return new AnimationDurations(
            0.07f,  // fire: 0.07 seconds - fast fire animation for automatic
            0.07f,  // aimFire: 0.07 seconds
            3.5f,   // reload: 3.5 seconds
            0.1f    // unholster: 0.1 seconds (from gun stats)
        );
    }
}

