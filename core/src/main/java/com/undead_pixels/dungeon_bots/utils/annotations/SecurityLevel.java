package com.undead_pixels.dungeon_bots.utils.annotations;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Different levels of security of increasing level
 */
public enum SecurityLevel {
    DEBUG(3, JsePlatform.debugGlobals()),
    AUTHOR(2, JsePlatform.standardGlobals()),
    DEFAULT(1, new Globals());
    public final int level;
    public final Globals globals;

    SecurityLevel(int level, Globals globals) {
        this.level = level;
        this.globals = globals;
    }
}
