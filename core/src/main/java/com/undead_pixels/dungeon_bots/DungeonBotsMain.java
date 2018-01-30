package com.undead_pixels.dungeon_bots;


import java.util.ArrayList;

import javax.swing.JFrame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.undead_pixels.dungeon_bots.ui.GDXandSwingScreen;
import com.undead_pixels.dungeon_bots.ui.GameView;




/**
 * The main class. Basically, all it does is point to the screen that we are
 * actually trying to render. WO: should also point to the world - this is a
 * class that gives us a handle on what we will be serializing and uploading to
 * the website, right?
 *
 */
public class DungeonBotsMain extends Game {

	/**
	 * Singleton instance
	 */
	public static final DungeonBotsMain instance = new DungeonBotsMain();

	private JFrame frame = null;

	/**
	 * private constructor for singleton
	 */
	private DungeonBotsMain() {
	}

	@Override
	public void setScreen(Screen screen) {
		if (this.screen != null && this.screen instanceof GDXandSwingScreen) {
			((GDXandSwingScreen) this.screen).attachScreenToFrame(null); // clear
																			// the
																			// current
																			// screen's
																			// frame
		}

		super.setScreen(screen);

		if (this.screen != null && this.screen instanceof GDXandSwingScreen) {
			((GDXandSwingScreen) this.screen).attachScreenToFrame(frame); // set
																			// the
																			// frame
																			// for
																			// the
																			// new
																			// screen
		}
	}

	/**
	 * Used to tell this to work with a specific JFrame (which likely contains
	 * the GDX canvas)
	 * 
	 * @param frame
	 */
	public void setFrame(JFrame frame) {
		if (frame != null) {
			this.frame = frame;
		}
	}

	@Override
	public void create() {
		// setScreen(new NullScreen());
		setScreen(new GameView());
	}

	/*
	 * ================================================================
	 * DungeonBotsMain USER IDENTIFICATION AND SECURITY STUFF
	 * ================================================================
	 */
	private User _CurrentUser = null;

	public User getUser() {
		return _CurrentUser;
	}

	public void setUser(User user) {
		if (user == _CurrentUser)
			return;
		_CurrentUser = user;
		user.setCurrentGame(this);
	}

	private ArrayList<String> _Authors = new ArrayList<String>();

	/** Returns whether the indicated user is an author of this game. */
	public boolean isAuthor(User user) {
		return _Authors.contains(user.getUserName());
	}

}
