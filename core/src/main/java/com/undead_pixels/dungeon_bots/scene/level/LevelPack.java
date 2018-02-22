package com.undead_pixels.dungeon_bots.scene.level;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;

/**
 * A collection of levels, potentially loaded from a file and sharable with
 * others.
 */
public class LevelPack {

	private transient static Serializer serializer = new Serializer();
	private transient World _DeserializedWorld = null;

	/**
	 * A set of rules that specify whether the player will be able to see some,
	 * all, or any of the levels in this Level Pack.
	 */
	public enum LevelVisibility {
		CURRENT, ALL, PRIOR
	}

	private HashSet<User> authors;
	private ArrayList<byte[]> levels;
	private int levelIndex = 0;
	private String name;
	private LevelVisibility visibilityRule;
	private User currentPlayer;
	private HashSet<Integer> playerCompleted;
	public UserScript transitionScript;

	public LevelPack(String name, User author) {
		this.name = name;

		this.transitionScript = new UserScript("onTransition",
				"--This script will be run to return an LuaInt specifying the next world to proceed to, given the current world state.",
				SecurityLevel.AUTHOR);

		this.levels = new ArrayList<byte[]>();
		this.levels.add(serializer.toBytes(new World(new File("default.lua"))));

		this.authors = new HashSet<User>();
		this.authors.add(author);

		visibilityRule = LevelVisibility.ALL;
		currentPlayer = null;
		playerCompleted = null;
	}

	/**
	 * Adds the author to this level pack. Returns whether the level pack was
	 * changed or not.
	 */
	public boolean addAuthor(User user) {
		return authors.add(user);
	}

	/**
	 * Returns whether the given user has author-level privileges for this level
	 * pack.
	 */
	public boolean isAuthor(User user) {
		return authors.contains(user);
	}

	/** Returns the current world. */
	public World getCurrentWorld() {
		if (_DeserializedWorld == null)
			_DeserializedWorld = serializer.toWorld(levels.get(levelIndex));
		return _DeserializedWorld;
	}

	/**
	 * Adds the given world to the Level Pack. Returns the index of the world
	 * added. If the world already exists on the Level Pack, returns the index
	 * of the world.
	 */
	public int saveWorld(World world) {
		throw new RuntimeException("Not implemented yet.");
	}

	/** Gets the Level Pack's name. */
	public String getName() {
		return this.name;
	}

	/** Sets the Level Pack's name. */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the current world to the index indicated. Throws an exception if the
	 * index is invalid.
	 */
	public World setCurrentWorld(int index) {
		if (index < 0 || index >= levels.size())
			throw new RuntimeException("Invalid level index: " + index);
		levelIndex = index;
		return getCurrentWorld();
	}

	/**
	 * Returns the rule determining which levels the player will have access to,
	 * based on what he has completed.
	 */
	public LevelVisibility getVisibilityRule() {
		return visibilityRule;
	}

	/**
	 * Sets the rule determining which levels the player will have access to,
	 * based on what he has completed.
	 */
	public void setVisibilityRule(LevelVisibility rule) {
		visibilityRule = rule;
	}

	/** Returns the current player, if one exists. If not, returns null. */
	public User getCurrentPlayer() {
		// TODO: implement this functionality.

		return currentPlayer;
	}

	/**
	 * Sets the player of this Level Pack. Once assigned, another player cannot
	 * be assigned.
	 */
	public void setCurrentPlayer(User newPlayer) {
		if (currentPlayer != null && !currentPlayer.equals(newPlayer))
			throw new RuntimeException("Once a player has been assigned to a Level Pack, it cannot be changed.");
		else
			currentPlayer = newPlayer;
	}

	/** Returns highest index number of worlds completed by this player. */
	public int getMaxCompleted() {
		int max = -1;
		for (Integer i : playerCompleted)
			max = Math.max(max, i);
		return max;
	}

	/** Returns whether a player has completed the given level index. */
	public boolean hasCompleted(int levelIndex) {
		return playerCompleted.contains(levelIndex);
	}

	/**
	 * Array of LuaScripts, with each LuaScript being the init/run sandbox of
	 * each level
	 */
	// private LuaInvocation[] levels;

	/**
	 * A sandbox that is applicable to the level pack as a whole
	 */
	// private LuaInvocation packScript;

	/**
	 * A collection of assets. Might be unused.
	 */
	// private HashMap<String, ByteBuffer> assets;

}
