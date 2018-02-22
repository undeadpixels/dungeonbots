package com.undead_pixels.dungeon_bots.script.annotations;

import com.undead_pixels.dungeon_bots.script.environment.GameGlobals;

import java.util.function.Supplier;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Enumerate different security levels for a DungeonBots user in a Script Environment
 */
public enum SecurityLevel {
    DEBUG(3, () -> JsePlatform.debugGlobals()),
    AUTHOR(2, () -> JsePlatform.standardGlobals()),
    DEFAULT(1, () -> GameGlobals.playerGlobals()),
    NONE(0, () -> JsePlatform.standardGlobals());
    public final int level;
    public final Supplier<Globals> globalsSupplier;

    SecurityLevel(int level, Supplier<Globals> globals) {
        this.level = level;
        this.globalsSupplier = globals;
    }
    
    public Globals getGlobals() {
    		return this.globalsSupplier.get();
    }
}
