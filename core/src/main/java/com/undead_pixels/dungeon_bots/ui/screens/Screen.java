package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowListener;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

import com.undead_pixels.dungeon_bots.scene.World;

/**
 * Defines GUI and control interface. A protected GUI state can be stored.
 * NOTE:  inheritance is from JFrame so that items contained can be focusable.
 */
public abstract class Screen extends JFrame {

	protected final World world;
	
	/** Will be used for saving GUI state, later. */
	protected final HashMap<String, Object> guiState = new HashMap<String, Object>();

	// =========================================================================
	// -------------------Screen CONSTRUCTORS-----------------------------------
	// =========================================================================

	protected Screen(World world) {
		super();
		this.world = world;
		
		//this.setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	    
		this._Controller = this.makeController();		
		this.setDefaultLayout();
		this.addComponents(this.getContentPane());	
	}

	/**
	 * This method is called at construction time to set the controller. It
	 * forces classes inheriting from Screen to override it, and in turn, to
	 * implement a ScreenController class.
	 */
	protected abstract ScreenController makeController();

	/**
	 * This method is called at construction time. Override the method to layout
	 * components for the game screen.
	 */
	protected abstract void addComponents(Container pane);

	/**
	 * Called at construction time to layout this screen. Override to specify
	 * the layout appearance. Can be called after construction time.
	 */
	protected abstract void setDefaultLayout();

	// =========================================================================
	// -------------------Screen CONTROL-----------------------------------
	// =========================================================================

	private ScreenController _Controller;

	/**
	 * Returns the current ScreenController. The default controller will simply
	 * call overrideable functions from the Screen class.
	 */
	protected ScreenController getController() {
		return _Controller;
	}

	/**
	 * The default controller simply calls overrideable functions from the
	 * Screen class. If a different controller is desired, it can be replaced.
	 */
	protected void setController(ScreenController controller) {		
		_Controller = controller;	
		
	}

	/**
	 * This controller is implemented separately so that classes inheriting from
	 * Screen will not be chock-full of irrelevant event handlers. This class
	 * must be extended in any class that inherits from Screen.
	 */
	protected abstract class ScreenController
			implements MouseInputListener, KeyListener, ActionListener, WindowListener, ChangeListener {
	}

}
