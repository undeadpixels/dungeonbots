package com.undead_pixels.dungeon_bots;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;

import com.undead_pixels.dungeon_bots.ui.Login;
import com.undead_pixels.dungeon_bots.ui.WorldView;
import com.undead_pixels.dungeon_bots.ui.screens.GDXandSwingScreen;
import com.undead_pixels.dungeon_bots.ui.screens.MainMenuScreen;

/**
 * The main class. Basically, all it does is point to the screen that we are
 * actually trying to render.
 *
 * A really stupid naming choice on libGDX's part, but this thing inheriting
 * from Game does not mean that this is related the part of the game that you
 * play. It does not contain a world or even care about the world. This class
 * just talks to whatever the active screen is (such as a GameScreen,
 * GameEditorScreen, CommunityScreen, and various full-screen menus, such as
 * MainMenuScreen) And then it passes rendering (and technically input) events
 * to them.
 */
public class DungeonBotsMain extends Game {

	/**
	 * Singleton instance
	 */
	public static final DungeonBotsMain instance = new DungeonBotsMain();

	/**
	 * The main frame for the entire game.
	 */
	private JFrame frame = null;
	
	/**
	 * The GDX canvas, stored here in case it is removed from the frame
	 */
	private Component glCanvas;

	/**
	 * private constructor for singleton
	 */
	private DungeonBotsMain() {
	}

	@Override
	public void setScreen(Screen screen) {

		// Clear the current screen's frame.
		if (this.screen != null && this.screen instanceof GDXandSwingScreen) {
			((GDXandSwingScreen) this.screen).attachScreenToFrame(null);
		}

		super.setScreen(screen);
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				//frame.add(glCanvas, BorderLayout.CENTER);
			}
		});

		// Set the frame for the new screen.
		if (this.screen != null && this.screen instanceof GDXandSwingScreen) {
			((GDXandSwingScreen) this.screen).attachScreenToFrame(frame);
		}
	}

	/**
	 * Used to tell this to work with a specific JFrame (which likely contains
	 * the GDX canvas)
	 * 
	 * @param frame
	 */
	public void setFrameAndCanvas(JFrame frame, Component canvas) {
		if (frame != null) {
			this.frame = frame;
			this.glCanvas = canvas;
		}
	}

	@Override
	public void create() {
		setScreen(new MainMenuScreen());
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
	 * TODO - the following commented-out code should be called by Community
	 * stuff, once that is implemented. However, this shouldn't be forced
	 * 
	 * in our design doc, we said:
	 * 
	 * An Internet connection will be required for access to the Sharing
	 * Platform, but this not required to run the pre-built parts of the game.
	 * 
	 * However, this will also be needed when we want to upload results to the
	 * server.
	 */
	public void requestLogin() {
		System.out.println("Starting login...");
		User user = Login.challenge("Welcome to DungeonBots.");
		if (user == null) {
			System.out.println("Invalid user login.  Closing program.");
			return;
		}
		System.out.println("Login valid.");
		DungeonBotsMain.instance.setUser(user);
	}

}
