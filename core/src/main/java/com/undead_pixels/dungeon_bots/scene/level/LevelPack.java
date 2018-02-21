package com.undead_pixels.dungeon_bots.scene.level;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaInvocation;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;

/**
 * A collection of levels, potentially loaded from a file and sharable with
 * others.
 */
public class LevelPack {
	/**
	 * Array of LuaScripts, with each LuaScript being the init/run sandbox of
	 * each level
	 */
	// private LuaInvocation[] levels;

	/**
	 * A sandbox that is applicable to the levelpack as a whole
	 */
	// private LuaInvocation packScript;

	/**
	 * A collection of assets. Might be unused.
	 */
	// private HashMap<String, ByteBuffer> assets;

	/**
	 * The set of User IDs that have author-level privileges for this LevelPack.
	 */
	private HashSet<Integer> authors;

	
	private HashSet<World> levels;

}
