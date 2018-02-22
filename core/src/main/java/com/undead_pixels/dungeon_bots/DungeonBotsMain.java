package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.ui.Login;
import java.awt.Image;
import java.io.File;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.ui.screens.Screen;
import com.undead_pixels.dungeon_bots.ui.screens.GameplayScreen;
import com.undead_pixels.dungeon_bots.ui.screens.LevelEditorScreen;
import com.undead_pixels.dungeon_bots.ui.screens.MainMenuScreen;

/**
 * The main game class. Maintains the identity of the current user and a
 * reference to the visual GUI currently on display. Has helper functions to
 * manage resources associated with the game.*
 */
public class DungeonBotsMain {

	/** The screen that is currently being shown. */
	private Screen _Screen;

	/** The World that is currently the focus of displayed screens. */
	private World _World;

	/** The LevelPack from which the current world is drawn. */
	private LevelPack _LevelPack;

	/** Returns the world currently associated with this game. */
	// public World getWorld() {
	// return _World;
	// }

	/**
	 * Sets the current world focus for the game. If a screen is currently being
	 * shown, rebuilds and displays the screen with the new world.
	 */
	public void setWorld(World world) {
		_World = world;

		if (_Screen instanceof GameplayScreen)
			setCurrentScreen(new GameplayScreen(_World));
		else if (_Screen instanceof LevelEditorScreen)
			setCurrentScreen(new LevelEditorScreen(_World));
		// else nothing.
	}

	/**
	 * Singleton instance. Only one DungeonBotsMain is capable of being
	 * constructed at a time. The game instance will be available statically in
	 * any Java code that imports this class.
	 */
	public static final DungeonBotsMain instance = new DungeonBotsMain();

	/**
	 * private constructor for singleton
	 */
	private DungeonBotsMain() {
		// Does nothing.
	}

	/*
	 * ================================================================
	 * DungeonBotsMain GAME MANAGEMENT STUFF
	 * ================================================================
	 */

	/**
	 * Starts the game. Startup will require a login first, and then go to the
	 * main menu screen.
	 */
	public void start() {

		// There will be no established screen at startup.
		if (_Screen != null)
			throw new RuntimeException("Multiple instances of the game cannot be run.");

		// Create a new world.
		_World = new World(new File("level1.lua"));

		// Fire up the main menu screen.
		setCurrentScreen(new MainMenuScreen());
	}

	/**
	 * Restart the game back to the main menu screen (but don't require a new
	 * login).
	 */
	public void restart() {
		throw new RuntimeException("Not implemented yet.");
	}

	/** Sets the current screen to the given screen. */
	public void setCurrentScreen(Screen newScreen) {

		// Remove the old screen.
		if (_Screen != null) {
			_Screen.dispose();
		}

		// Sanity check.
		assert newScreen != null;

		if (!(newScreen instanceof MainMenuScreen)) {
			if (getUser() == null && !requestLogin(3))
				System.exit(0);
			if (_LevelPack==null){
				_LevelPack = new LevelPack("My Level Pack", getUser());
			}
		}

		// If there is no valid login, just return.
		if (!(newScreen instanceof MainMenuScreen) && getUser() == null && !requestLogin(3))
			return;

		// Start the new screen.
		_Screen = newScreen;
		// _Screen.pack();
		_Screen.setVisible(true);
	}

	/**
	 * Sets the current screen to a GameplayScreen to play the current world.
	 */
	public void setGameplayScreen() {
		setCurrentScreen(new GameplayScreen(_World));
	}

	/**
	 * Sets the current screen to a LevelEditorScreen to edit the current world.
	 */
	public void setLevelEditorScreen() {
		setCurrentScreen(new LevelEditorScreen(_World));
	}

	public void setResultScreen() {
		throw new RuntimeException("Not implemented yet.");
	}

	/** Returns the current level pack. */
	public LevelPack getLevelPack() {
		return _LevelPack;
	}

	/*
	 * ================================================================
	 * DungeonBotsMain USER IDENTIFICATION AND SECURITY STUFF
	 * ================================================================
	 */
	private User currentUser = null;

	/**
	 * @return The current user
	 */
	public User getUser() {
		return currentUser;
	}

	/**
	 * Sets the current user
	 * 
	 * @param user
	 *            The user
	 */
	public void setUser(User user) {
		if (user == currentUser)
			return;
		currentUser = user;
		user.setCurrentGame(this);
	}

	/**
	 * A function that prompts the user to log in.
	 * 
	 * An Internet connection will be required for access to the Sharing
	 * Platform, but this not required to run the pre-built parts of the game.
	 * 
	 * However, this will also be needed when we want to upload results to the
	 * server, or to publish a game to the server.
	 * 
	 * 
	 * @return Returns true if login was successful. Otherwise, returns false.
	 */
	public boolean requestLogin(int attempts) {
		System.out.println("Starting login...");
		User user = Login.challenge("Welcome to DungeonBots.", attempts);
		if (user == null) {
			System.out.println("Invalid user login.");
			return false;
		}
		System.out.println("Login valid.");
		setUser(user);
		return true;
	}

	/** Closes the game, releases all visual pieces. */
	public void dispose() {
		if (_Screen != null)
			_Screen.dispose();
	}

}
