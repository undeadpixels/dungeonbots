package com.undead_pixels.dungeon_bots.scene.level;

import java.awt.Image;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;

/**
 * A collection of levels, potentially loaded from a file and sharable with
 * others.
 */
public class LevelPack {

	public static final String EXTENSION = "json";
	private static final String DEFAULT_MAIN_EMBLEM = "images/shoes_hat.jpg";
	private static final String DEFAULT_EMBLEM = "images/ice_cave.jpg";
	public static final String UNKNOWN_AUTHOR_NAME = "Unknown author";
	
	public static final int EMBLEM_WIDTH = 300;
	public static final int EMBLEM_HEIGHT = 200;
	


	public enum FeedbackModel {
		RATING_AND_COMMENTS, SCORES, SAVED_GAMES
	}


	private String name;
	private User currentPlayer;
	private final HashSet<Integer> playerVisible;
	public UserScript transitionScript;
	private Image mainEmblem;
	private String description = null;

	private WorldList levels;
	private int levelIndex = 0;
	private int levelCount = -1;
	private ArrayList<Image> levelEmblems;
	private ArrayList<String> levelTitles;
	private ArrayList<String> levelDescriptions;

	private final User originalAuthor;
	private final ArrayList<User> authors;
	private final ArrayList<User> audienceUsers;
	private final ArrayList<Integer> audienceGroups; // TODO: implement a group
														// class?
	private final LocalDateTime creationDate;
	private LocalDateTime publishEnd;
	private LocalDateTime publishStart;
	private FeedbackModel feedbackModel;
	private boolean isLocked;


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

		if (worlds.length == 0)
			worlds = new World[] { new World(new File("default.lua")) };
		this.levels = new WorldList();
		this.levels.addAll(Arrays.asList(worlds));
		this.levelIndex = 0;
		this.levelCount = worlds.length;
		this.levelEmblems = new ArrayList<Image>();
		this.levelTitles = new ArrayList<String>();
		this.levelDescriptions = new ArrayList<String>();

		this.authors = new ArrayList<>();
		this.originalAuthor = author;
		this.mainEmblem = UIBuilder.getImage(DEFAULT_MAIN_EMBLEM);
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
	 * pack.  If the indicated user is null, returns false.
	 */
	public boolean isAuthor(User user) {
		if (user == null)
			return false;
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


	public LocalDateTime getPublishEnd() {
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


	/**The main emblem is an image that represents this level pack.*/
	public Image getEmblem() {
		if (mainEmblem == null)
			mainEmblem = UIBuilder.getImage(DEFAULT_MAIN_EMBLEM);
		return mainEmblem;
	}


	/**The main emblem is an image that represents this level pack.*/
	public void setEmblem(Image image) {
		mainEmblem = image;
	}


	public String getDescription() {
		return (description == null) ? "(No description)" : description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	/**A LevelPack which is locked should only be modified by Users who are identified as 
	 * authors.*/
	public boolean getLocked() {
		return isLocked;
	}


	/**Determines whether only an identified author User should be allowed to modify this 
	 * LevelPack.*/
	public void setLocked(boolean value) {
		isLocked = value;
	}


	// ============================================================
	// ========= LevelPack WORLD/LEVEL STUFF ======================
	// ============================================================

	/**Returns whether this pack is only a partial pack.  A pack may be partial if it was serialized for 
	 * purposes of making a list, where deserializing the worlds themselves may be wasteful.*/
	public boolean isPartial() {
		return levelCount != levels.size();
	}


	public int getLevelIndex() {
		return levelIndex;
	}


	public int getLevelCount() {
		return levelCount;
	}


	public void setLevelIndex(int newIndex) {
		levelIndex = newIndex;
	}


	/**The emblem is an image representing a world/level.*/
	public Image getLevelEmblem(int index) {
		return levelEmblems.get(index);
	}


	/**Sets the emblem representing a world or level.*/
	public void setLevelEmblem(int index, Image img) {
		levelEmblems.set(index, img);
	}


	public String getLevelTitle(int index) {
		return levelTitles.get(index);
	}


	public void setLevelTitle(int index, String title) {
		levelTitles.set(index, title);
	}


	public String getLevelDescription(int index) {
		return levelDescriptions.get(index);
	}


	public void setLevelDescription(int index, String description) {
		levelDescriptions.set(index, description);
	}


	// ============================================================
	// ========= LevelPack SERIALIZATION STUFF ====================
	// ============================================================


	/** Constructs and returns a LevelPack from the indicated file. */
	public static LevelPack fromFile(String filename) {
		String json = Serializer.readStringFromFile(filename);
		LevelPack lp = fromJson(json);
		standardize(lp);
		return lp;
	}


	/** Constructs and returns a LevelPack from the indicated json String. */
	public static LevelPack fromJson(String json) {
		return Serializer.deserializeLevelPack(json);
	}


	/**Constructs a LevelPack object, but does not deserialize the bytes representing 
	 * the Worlds.  Useful for constructing a list of LevelPack objects.*/
	public static LevelPack fromFilePartial(String filename) {

		// Read everything from the LevelPack, but don't bother deserializing
		// the Worlds.
		String json = Serializer.readStringFromFile(filename);
		return fromJsonPartial(json);
	}


	/**Constructs a LevelPack object from the given JSON, but does not serialize the bytes 
	 * representing the Worlds.  Useful for constructing a list of LevelPack objects.*/
	public static LevelPack fromJsonPartial(String json) {
		LevelPack partial = Serializer.deserializePartialLevelPack(json);
		if (partial == null)
			return partial;

		standardize(partial);

		// Return the partial LevelPack.
		return partial;
	}


	/** Brings the data contained in the LevelPack up-to-date, in case stuff is missing.*/
	private static void standardize(LevelPack pack) {
		// TODO: kill this once we have the LevelPacks stored at a standardized
		// state.

		// If level header info was not stored originally, check to make sure
		// there are not nulls after the deserialization.
		if (pack.levels == null)
			pack.levels = new WorldList();
		if (pack.levelTitles == null)
			pack.levelTitles = new ArrayList<String>();
		if (pack.levelDescriptions == null)
			pack.levelDescriptions = new ArrayList<String>();
		if (pack.levelEmblems == null)
			pack.levelEmblems = new ArrayList<Image>();

		// If the level header stuff wasn't originally stored, it may be
		// necessary to populate header info with dummy data. There will always
		// be at least one level title/description/emblem, even if the levels
		// themselves don't exist.
		pack.levelCount = Math.max(1, Math.max(pack.levels.size(),
				Math.max(pack.levelTitles.size(), Math.max(pack.levelDescriptions.size(), pack.levelEmblems.size()))));
		while (pack.levelTitles.size() < pack.levelCount)
			pack.levelTitles.add("Unnamed level.");
		while (pack.levelDescriptions.size() < pack.levelCount)
			pack.levelDescriptions.add("No description.");
		while (pack.levelEmblems.size() < pack.levelCount)
			pack.levelEmblems.add(UIBuilder.getImage("images/ice_cave.jpg"));

		// The lock is something new added, but it defaults to false anyway.

	}


	/** Saves this LevelPack to the indicated file name. */
	public void toFile(String filename) {
		String serialized = Serializer.serializeLevelPack(this);
		Serializer.writeToFile(filename, serialized.getBytes());
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
