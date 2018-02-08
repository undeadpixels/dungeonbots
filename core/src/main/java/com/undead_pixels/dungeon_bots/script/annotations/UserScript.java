package com.undead_pixels.dungeon_bots.script.annotations;

import com.undead_pixels.dungeon_bots.scene.IWorld;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.LuaScript;

/**
 * A user script associates a Lua script with the information pertaining to how
 * it will be used. For example, it will include info including the underlying
 * String representation of the script, information about the sections which are
 * or are not player-editable, and a function which can be overridden to
 * determine whether or not the script will be executed on a particular pass
 * through the game loop.
 * 
 * When we implement the onStart script, the player's onBotNear script, etc., we
 * will inherit from this and add to the entity's collection of scripts.
 */
public class UserScript {

	public static final int PLAYER_READ = 1;
	public static final int PLAYER_EXECUTE = 2;
	public static final int PLAYER_WRITE = 4;
	public static final int PlAYER_FULL_ACCESS = PLAYER_READ | PLAYER_EXECUTE | PLAYER_WRITE;

	public String code;
	public Object editability; // Still working on this.
	public int accessLevel;
	public String name;

	public UserScript(String name, String code, int accessLevel) {
		this.name = name;
		this.code = code;
		this.accessLevel = accessLevel;
	}

	@Deprecated
	/**
	 * Determines whether or not this user script will execute on this pass
	 * through the game loop. Can be overridden in a derived class.
	 */
	public boolean canExecute(World world, long time) {
		return true;
	}
	

	/**
	 * Determines whether or not this user script will execute on this pass
	 * through the game loop. Can be overridden in a derived class.
	 */
	public boolean canExecute(IWorld world, long time){
		return true;
	}

	@Override
	public String toString() {
		return "Script: " + name;
	}

	/**
	 * Returns a LuaScript living in the given sandbox, from the contained code.
	 */
	public final LuaScript toLuaScript(LuaSandbox sandbox) {
		return sandbox.script(code);
	}
}
