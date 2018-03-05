package com.undead_pixels.dungeon_bots.scene.level;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.stream.Stream;

import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

/**
 * A collection of levels, potentially loaded from a file and sharable with
 * others.
 */
public class LevelPack {

	public enum FeedbackModel {
		RATING_AND_COMMENTS, SCORES, SAVED_GAMES
	}

	private WorldList levels;
	private int levelIndex = 0;
	private String name;
	private User currentPlayer;
	private HashSet<Integer> playerVisible;
	public UserScript transitionScript;

	private final User originalAuthor;
	private ArrayList<User> authors;
	private ArrayList<User> audienceUsers;
	private ArrayList<Integer> audienceGroups; // TODO: implement a group class?
	private final LocalDateTime creationDate;
	private LocalDateTime publishEnd;
	private LocalDateTime publishStart;
	private FeedbackModel feedbackModel;

	/**
	 * Creates a new Level Pack, having the given name and the first specified
	 * author. The transition script will simple be a default script, and the
	 * first World will be a default first World.
	 */
	public LevelPack(String name, User author, World... worlds) {
		this.name = name;

		this.transitionScript = new UserScript("onTransition",
				"--This script will be run to return an LuaInt specifying the next world to proceed to, given the current world state.",
				SecurityLevel.AUTHOR);

		this.levels = new WorldList();
		//this.levels.add(new World(new File("default.lua")));
		if(worlds.length == 0)
			worlds = new World[] { new World(new File("default.lua")) };

		this.levels.addAll(Arrays.asList(worlds));
		this.levelIndex = 0;

		this.authors = new ArrayList<>();
		this.originalAuthor = author;
		addAuthor(author);
		creationDate = LocalDateTime.now();
		publishStart = LocalDateTime.now();
		publishEnd = publishStart.plusYears(1);
		audienceUsers = new ArrayList<>();
		audienceGroups = new ArrayList<>();
		feedbackModel = FeedbackModel.RATING_AND_COMMENTS;

		currentPlayer = null;
		playerVisible = new HashSet<>();
		playerVisible.add(0);
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
	 * The transition script is the script by which one level leads to another.
	 */
	public UserScript getTransitionScript() {
		return this.transitionScript;
	}

	/**
	 * The transition script is the script by which one level leads to another.
	 * It should return an integer value given a particular world state.
	 */
	public void setTransitionScript(UserScript script) {
		this.transitionScript = script;
	}

	// ============================================================
	// ========= LevelPack PLAYER VIEW MANAGEMENT STUFF ===========
	// ============================================================

	/** Returns the current world. */
	public World getCurrentWorld() {
		assert levelIndex >= 0 && levelIndex < levels.size();
		return levels.get(levelIndex);
	}

	/** Returns the index of the current World. */
	public int getCurrentWorldIndex() {
		return levelIndex;
	}

	/** Returns an array of all Worlds in this LevelPack. */
	public World[] getAllWorlds() {
		return levels.toArray(new World[levels.size()]);
	}

	/**
	 * Sets the current world to the index indicated. Throws an exception if the
	 * index is invalid.
	 */
	public World setCurrentWorld(int index) {
		assert index >= 0 && index < levels.size();
		return levels.get(levelIndex = index);
	}

	/** Returns the current player, if one exists. If not, returns null. */
	public User getCurrentPlayer() {
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
	}

	/** Returns highest index number of worlds completed by this player. */
	public int getMaxCompleted() {
		int max = -1;
		for (Integer i : playerVisible)
			max = Math.max(max, i);
		return max;
	}

	/**
	 * Returns whether a player can see the given World index in the TODO: Level
	 * Pack Screen.
	 */
	public boolean isPlayerVisible(int levelIndex) {
		return playerVisible.contains(levelIndex);
	}

	/**
	 * Adds the given World index for the player in the TODO: Level Pack Screen.
	 */
	public boolean addPlayerVisible(Integer levelIndex) {
		return playerVisible.add(levelIndex);
	}

	/**
	 * Removes the given World index from the player in the TODO: Level Pack
	 * Screen.
	 */
	public boolean removePlayerVisible(Integer levelIndex) {
		return playerVisible.remove(levelIndex);
	}

	/** Gets the set of all level indices that are visible to the player. */
	public Integer[] getAllPlayerVisible() {
		return playerVisible.toArray(new Integer[playerVisible.size()]);
	}

	// ============================================================
	// ========= LevelPack PUBLICATION STUFF ======================
	// ============================================================

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

	public User getOriginalAuthor() {
		return originalAuthor;
	}

	public User[] getAllAudienceUsers() {
		return audienceUsers.toArray(new User[audienceUsers.size()]);
	}

	public boolean getIsAudience(User user) {
		return audienceUsers.contains(user);
	}

	public boolean addUser(User user) {
		if (audienceUsers.contains(user))
			return false;
		audienceUsers.add(user);
		return true;
	}

	public boolean removeUser(User user) {
		return audienceUsers.remove(user);
	}

	public boolean getIsAudienceGroup(Integer id) {
		return audienceGroups.contains(id);
	}

	public boolean addAudienceGroup(Integer id) {
		if (audienceGroups.contains(id))
			return false;
		audienceGroups.add(id);
		return true;
	}

	public Integer[] getAllAudienceGroups() {
		return audienceGroups.toArray(new Integer[audienceGroups.size()]);
	}

	public boolean removeAudienceGroup(Integer id) {
		return audienceGroups.remove(id);
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public LocalDateTime getPublishStart() {
		return publishStart;
	}

	public void setPublicationStart(LocalDateTime start) {
		publishStart = start;
	}

	public LocalDateTime getPublicateEnd() {
		return publishEnd;
	}

	public void setPublicationEnd(LocalDateTime end) {
		publishStart = end;
	}

	public FeedbackModel getFeedbackModel() {
		return feedbackModel;
	}

	public void setFeedbackModel(FeedbackModel model) {
		feedbackModel = model;
	}

	// ============================================================
	// ========= LevelPack SERIALIZATION STUFF ====================
	// ============================================================

	/** Constructs and returns a LevelPack from the indicated file. */
	public static LevelPack fromFile(String filename) {
		String json = Serializer.readStringFromFile(filename);
		return fromJson(json);
	}

	/** Saves this LevelPack to the indicated file name. */
	public void toFile(String filename) {
		String serialized = Serializer.serializeLevelPack(this);
		Serializer.writeToFile(filename, serialized.getBytes());
	}

	/** Constructs and returns a LevelPack from the indicated json String. */
	public static LevelPack fromJson(String json) {
		return Serializer.deserializeLevelPack(json);
	}

	/** Returns a String which is the json serialization of this LevelPack. */
	public String toJson() {
		return Serializer.serializeLevelPack(this);
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
