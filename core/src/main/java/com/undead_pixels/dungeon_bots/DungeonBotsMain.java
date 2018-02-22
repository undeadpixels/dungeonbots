package com.undead_pixels.dungeon_bots;


import java.io.File;

import javax.swing.JFrame;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.ui.Login;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.screens.Screen;
import com.undead_pixels.dungeon_bots.ui.screens.GameplayScreen;
import com.undead_pixels.dungeon_bots.ui.screens.LevelEditorScreen;
import com.undead_pixels.dungeon_bots.ui.screens.MainMenuScreen;
import com.undead_pixels.dungeon_bots.ui.screens.ResultsScreen;

/**
 * The main game class. Maintains the identity of the current user and a
 * reference to the visual GUI currently on display. Has helper functions to
 * manage resources associated with the game.*
 */
public class DungeonBotsMain {

	public enum ScreenType {
		GAMEPLAY, LEVEL_EDITOR, MAIN_MENU, RESULTS
	}

	/** The screen that is currently being shown. */
	private Screen _Screen;

	/** The LevelPack from which the current world is drawn. */
	private LevelPack _LevelPack;

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

		// Fire up the main menu screen.
		setCurrentScreen(ScreenType.MAIN_MENU);
	}

	/**
	 * Restart the game back to the main menu screen (but don't require a new
	 * login).
	 */
	public void restart() {
		throw new RuntimeException("Not implemented yet.");
	}

	/** Sets the current screen to the given screen. */
	public void setCurrentScreen(ScreenType screenType) {

		// Remove the old screen.
		if (_Screen != null) {
			_Screen.dispose();
		}

		// Build the appropriate type of new screen.
		switch (screenType) {
		case MAIN_MENU:			
			_Screen = new MainMenuScreen();
			break;
		case GAMEPLAY:
			if (getUser() == null && !requestLogin(3))
				System.exit(0);
			if (_LevelPack == null)
				_LevelPack = new LevelPack("My Level Pack", getUser());
			if (_LevelPack.getCurrentPlayer() != null && !_LevelPack.getCurrentPlayer().equals(getUser())) {
				throw new RuntimeException("Cannot switch to a game being played by another player.");
			}
			_LevelPack.setCurrentPlayer(getUser());
			_Screen = new GameplayScreen(_LevelPack.getCurrentWorld());
			break;
		case LEVEL_EDITOR:
			if (getUser() == null && !requestLogin(3))
				System.exit(0);
			if (_LevelPack == null)
				_LevelPack = new LevelPack("My Level Pack", getUser());		
			//World breakit = new World(new File("maze2.lua"));
			_Screen = new LevelEditorScreen(_LevelPack.getCurrentWorld());
			break;
		case RESULTS:
			_Screen = new ResultsScreen(_LevelPack.getCurrentWorld());
		default:
			throw new RuntimeException("Have not implemented switch to screen type: " + screenType.toString());
		}

		// Run the new screen.
		// _Screen.pack();
		_Screen.setVisible(true);
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
		//JFrame f = UIBuilder.makeLoginFrame();
		//f.setVisible(true);
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
