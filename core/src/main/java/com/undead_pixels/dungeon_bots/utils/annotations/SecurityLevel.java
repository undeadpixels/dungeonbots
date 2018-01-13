package com.undead_pixels.dungeon_bots.utils.annotations;

/**
 * Different levels of security of increasing level
 */
public enum SecurityLevel {
    DEBUG(4),
    AUTHOR(3),
    ADVANCED(2),
    SIMPLE(1);
    public final int level;

    SecurityLevel(int level) {
        this.level = level;
    }
}
