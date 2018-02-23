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

	/**
	 * Creates a new Level Pack, having the given name and the first specified
	 * author. The transition script will simple be a default script, and the
	 * first World will be a default first World.
	 */
	public LevelPack(String name, User author) {
		this.name = name;

		this.transitionScript = new UserScript("onTransition",
				"--This script will be run to return an LuaInt specifying the next world to proceed to, given the current world state.",
				SecurityLevel.AUTHOR);

		this.levels = new ArrayList<byte[]>();
		_DeserializedWorld = new World(new File("default.lua"));
		this.levels.add(serializer.toBytes(_DeserializedWorld));

		this.authors = new HashSet<User>();
		addAuthor(author);

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

	/**
	 * Returns an array of all the authors contained in this Level Pack. Note
	 * that the array returned is NOT a reference to the collection stored
	 * internally.
	 */
	public User[] getAllAuthors() {
		return authors.toArray(new User[authors.size()]);
	}

	/** Returns the current world. */
	public World getCurrentWorld() {
		if (_DeserializedWorld == null)
			_DeserializedWorld = serializer.toWorld(levels.get(levelIndex));
		return _DeserializedWorld;
	}

	/** Gets the Level Pack's name. */
	public String getName() {
		return this.name;
	}

	/** Sets the Level Pack's name. */
	public void setName(String name) {
		this.name = name;
	}

	/** Saves the currently-operating world to the Level Pack. */
	public void saveCurrentWorld() {
		assert levelIndex >= 0 && levelIndex < levels.size();
		levels.set(levelIndex, serializer.toBytes(_DeserializedWorld));
	}

	/**
	 * Sets the current world to the index indicated. Throws an exception if the
	 * index is invalid.
	 */
	public World setCurrentWorld(int index) {
		if (index == levelIndex)
			return _DeserializedWorld;
		assert levelIndex >= 0 && levelIndex < levels.size();
		// TODO: dispose of the running World?
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

		if (currentPlayer != null) {
			if (currentPlayer.equals(newPlayer))
				return;
			else if (newPlayer == null)
				throw new RuntimeException("Once a player has been assigned to a Level Pack, it cannot be nulled.");
			else
				throw new RuntimeException("Once a player has been assigned to a Level Pack, it cannot be changed.");
		} else if (newPlayer == null)
			return;
		currentPlayer = newPlayer;
		playerCompleted = new HashSet<Integer>();
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
