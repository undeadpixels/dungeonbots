package com.undead_pixels.dungeon_bots;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.function.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.undead_pixels.dungeon_bots.ui.GDXandSwingScreen;
import com.undead_pixels.dungeon_bots.ui.GameView;
import com.undead_pixels.dungeon_bots.ui.MainMenuScreen;

import javax.swing.*;
import javax.swing.text.rtf.RTFEditorKit;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pool;
import com.undead_pixels.dungeon_bots.ui.DropDownMenu;
import com.undead_pixels.dungeon_bots.ui.DropDownMenuStyle;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.syntaxkits.LuaSyntaxKit;

/*
import jsyntaxpane.syntaxkits.LuaSyntaxKit;

import com.undead_pixels.dungeon_bots.libraries.jsyntaxpane.*;
import com.undead_pixels.dungeon_bots.libraries.jsyntaxpane.syntaxkits.*;
*/

/**
 * The main class. Basically, all it does is point to the screen that we are
 * actually trying to render.
 *
 */
public class DungeonBotsMain extends Game {

	private User _CurrentUser = null;

	public User getUser() {
		return _CurrentUser;
	}

	public void setUser(User user) {
		if (_CurrentUser != null)
			throw new IllegalStateException("Game's user can be set only once.");
		_CurrentUser = user;
	}

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
		//setScreen(new MainMenuScreen());
	}

}
