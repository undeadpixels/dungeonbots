package com.undead_pixels.dungeon_bots.script.annotations;

import java.awt.List;
import java.io.Serializable;
import java.util.ArrayList;

import com.undead_pixels.dungeon_bots.math.IntegerSet;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;

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
public class UserScript implements Serializable {

	public String code;
	public SecurityLevel level;
	public String name;
	public ArrayList<IntegerSet.Interval> locks;

	public UserScript(String name, String code) {
		this(name, code, SecurityLevel.DEFAULT);
	}

	public UserScript(String name, String code, SecurityLevel level) {
		this.name = name;
		this.code = code;
		this.level = level;
		this.locks = new ArrayList<IntegerSet.Interval>();
	}

	/** Returns a copy of this UserScript. */
	public UserScript copy() {
		UserScript ret = new UserScript(this.name, this.code, this.level);
		for (IntegerSet.Interval lock : this.locks)
			ret.locks.add(new IntegerSet.Interval(lock.start, lock.end));
		// ret.locks = new IntegerIntervalSet(this.locks);
		return ret;
	}

	@Deprecated
	/**
	 * Determines whether or not this user script will execute on this pass
	 * through the game loop. Can be overridden in a derived class.
	 */
	public boolean canExecute(World world, long time) {
		return true;
	}

	@Override
	public String toString() {
		return "Script: " + name;
	}

}
