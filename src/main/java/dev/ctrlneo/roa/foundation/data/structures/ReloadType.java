package dev.ctrlneo.roa.foundation.data.structures;

/**
 * Defines how a gun reloads.
 */
public enum ReloadType {
    /**
     * One-shot reload - entire magazine is refilled at once at the end of animation.
     * Example: Mag swap + rack bolt
     */
    ONE_SHOT,
    
    /**
     * Sequential reload - magazine is refilled in batches/rounds progressively.
     * Each sequence adds rounds to the magazine.
     * Can be interrupted, keeping the rounds loaded so far.
     * Example: Shotgun shell-by-shell, 3-round stripper clip
     */
    SEQUENTIAL
}

