package com.undead_pixels.dungeon_bots;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.ui.Login;
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
	private World _World;
	private Vector<Entity> _EntityPalette;

	/** Returns the world currently associated with this game. */
	public World getWorld() {
		return _World;
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

		// Set up the palettes. TODO: eventually, this data will not be
		// populated in the constructor, but instead will come from a download
		// or from specs in a LevelPack.
		setupPalettes();

	}

	/**
	 * Returns a references to the palette of entities associated with this
	 * game.
	 */
	public Vector<Entity> getEntityPalette() {
		return _EntityPalette;
	}

	private void setupPalettes() {
		// Set up the bots available.
		_EntityPalette = new Vector<Entity>();
		//_EntityPalette.add(Player.worldlessPlayer());

		// Set up the dynamic stuff available.
		// Nothing right now.
		

		// Set up the static stuff available.
		//_EntityPalette.add(Tile.worldlessTile("floor", false));
		//_EntityPalette.add(Tile.worldlessTile("wall", true));
		//_EntityPalette.add(Tile.worldlessTile("goal", false));
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

		// If there is no valid login, just return.
		if (!requestLogin(3))
			return;

		// Create a new world.
		_World = new World(new File("sample-level-packs/sample-pack-1/levels/level2.lua"));

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

		// Start the new screen.
		_Screen = newScreen;
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

	/*
	 * ================================================================
	 * DungeonBotsMain RESOURCE CONTROL
	 * ================================================================
	 */

	/** Gets an ImageIcon based on the image at the given location. */
	public static Image getImage(String filename) {
		String path = System.getProperty("user.dir") + "/images/" + filename;
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException ioex) {
			System.err.println("System resource missing: " + path);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return img;
	}
}
