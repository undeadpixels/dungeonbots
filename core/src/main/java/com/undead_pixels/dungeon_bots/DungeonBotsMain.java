package com.undead_pixels.dungeon_bots;

import javax.swing.JFrame;

import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.auth.LoginService;

import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.screens.Screen;
import com.undead_pixels.dungeon_bots.ui.screens.MainMenuScreen;

/**
 * The main game class. Maintains the identity of the current user and a
 * reference to the visual GUI currently on display. Has helper functions to
 * manage resources associated with the game.*
 */
public class DungeonBotsMain {

	/** The screen that is currently being shown. */
	private Screen _Screen;

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

		_Screen = newScreen;
		// Run the new screen.
		// _Screen.pack();
		_Screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_Screen.setup();
		_Screen.setVisible(true);
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
	}

	/**
	 * A function that prompts the user to log in.
	 * 
	 * TODO: An Internet connection will be required for access to the Sharing
	 * Platform, but this not required to run the pre-built parts of the game.
	 * 
	 * However, this will also be needed when we want to upload results to the
	 * server, or to publish a game to the server.
	 * 
	 * 
	 * @return Returns true if login was successful. Otherwise, returns false.
	 */
	public boolean requestLogin(String message, int attempts) {

		LoginService service = new LoginService() {
			@Override
			public boolean authenticate(String name, char[] password, String server) throws Exception {
				// TODO: actually go online and attempt to authenticate.
				return true;
			}
		};

		return UIBuilder.showLoginModal(message, null, null, null, service) == Status.SUCCEEDED;

	}

	/** Closes the game, releases all visual pieces. */
	public void dispose() {
		if (_Screen != null)
			_Screen.dispose();
	}

}
