package com.undead_pixels.dungeon_bots.scene;

import java.nio.ByteBuffer;
import java.util.HashMap;

import com.undead_pixels.dungeon_bots.script.LuaScript;

/**
 * A collection of levels, potentially loaded from a file and sharable with others.
 */
public class LevelPack {
	/**
	 * Array of LuaScripts, with each LuaScript being the init/run scriptEnv of each level
	 */
	private LuaScript[] levels;

	/**
	 * A scriptEnv that is applicable to the levelpack as a whole
	 */
	private LuaScript packScript;
	
	/**
	 * A collection of assets. Might be unused.
	 */
	private HashMap<String, ByteBuffer> assets;
}
