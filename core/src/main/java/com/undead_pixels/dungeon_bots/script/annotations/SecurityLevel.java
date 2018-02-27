package com.undead_pixels.dungeon_bots.script.annotations;

import com.undead_pixels.dungeon_bots.script.environment.GameGlobals;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Enumerate different security levels for a DungeonBots user in a Script Environment
 */
public enum SecurityLevel {
	/**
	 * Only the most privileged things can use these functions
	 */
	DEBUG(1000, JsePlatform.debugGlobals()),

	/**
	 * Only authors can use these
	 */
	AUTHOR(100, JsePlatform.standardGlobals()),

	/**
	 * Only authors or the proper entity can use these
	 */
	ENTITY(50, GameGlobals.playerGlobals()),

	/**
	 * Only authors or the proper team can use these
	 */
	TEAM(10, GameGlobals.playerGlobals()),

	/**
	 * Anyone can use these
	 */
	DEFAULT(0, GameGlobals.playerGlobals()),

	/**
	 * Anyone can use these
	 */
	NONE(0, JsePlatform.standardGlobals());
	public final int level;
	public final Globals globals;

	SecurityLevel(int level, Globals globals) {
		this.level = level;
		this.globals = globals;
	}
}
