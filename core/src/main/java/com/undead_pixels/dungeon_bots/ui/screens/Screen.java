package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 * Defines GUI and control interface. A protected GUI state can be stored.
 * NOTE:  inheritance is from JFrame so that items contained can be focusable.
 */
public abstract class Screen extends JFrame {

	/** Will be used for saving GUI state, later. */
	protected final HashMap<String, Object> guiState = new HashMap<String, Object>();

	// =========================================================================
	// -------------------Screen CONSTRUCTORS-----------------------------------
	// =========================================================================

	protected Screen() {
		super();
		
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
			implements MouseListener, KeyListener, MouseMotionListener, ActionListener {
	}

	/*
	 * // TODO: the layout-type functions we have written ("addPane", etc.) //
	 * essentially just reproduce the visual layout stored in components and //
	 * such. Using these other layout-type functions obfuscates what's going on.
	 * 
	 *//**
		 * Internal reference to the frame the GDX's context (and any side
		 * panels this owns) lives in.
		 */
	/*
	 * private JFrame frame;
	 * 
	 *//**
		 * The top menu bar for File, Edit, ... (if any).
		 */
	/*
	 * private JMenuBar menuBar;
	 * 
	 *//**
		 * A map of which sides various panes are attached where on the main
		 * window.
		 */
	/*
	 * private HashMap<String, JComponent> sidePanes = new HashMap<>();
	 * 
	 *//**
		 * A map of names for overlaying windows vs their contents. The windows
		 * are stored implicitly in component.getParent().
		 */
	/*
	 * private HashMap<JComponent, String> overlayWindowComponentsNames = new
	 * HashMap<>();
	 * 
	 *//**
		 * A map of menus for each of the overlaying windows.
		 */
	/*
	 * private HashMap<JComponent, JMenuBar> otherWindowMenus = new HashMap<>();
	 * 
	 *//**
		 * This should only be called by DungeonBotsMain.
		 * 
		 * It just signals that this screen is either becoming active (and being
		 * attached to the given JFrame) or is becoming inactive and should
		 * remove any children from the JFrame it was previously attached to.
		 * 
		 * @param frame
		 */
	/*
	 * public final void attachScreenToFrame(JFrame frame) { if (frame == null)
	 * {
	 * 
	 * // Detach any children from the old frame frame.removeAll();
	 * 
	 * 
	 * // Destroy any child windows for (JComponent c :
	 * overlayWindowComponentsNames.keySet()) { Container p = c.getParent(); if
	 * (p != null) { p.setVisible(false); p.remove(c); } }
	 * 
	 * this.frame.setJMenuBar(null); this.frame.revalidate(); } else { // Attach
	 * any children to the new frame for (String s : sidePanes.keySet()) {
	 * frame.add(sidePanes.get(s), s); }
	 * 
	 * // Create/show any child windows for (JComponent c :
	 * overlayWindowComponentsNames.keySet()) { String title =
	 * overlayWindowComponentsNames.get(c); JMenuBar menu =
	 * otherWindowMenus.get(c); JDialog d = new JDialog(frame, title); d.add(c);
	 * d.setJMenuBar(menu); d.pack(); int locX = frame.getLocationOnScreen().x +
	 * frame.getWidth() / 2 - d.getWidth() / 2; int locY =
	 * frame.getLocationOnScreen().y + frame.getHeight() / 2 - d.getHeight() /
	 * 2; d.setLocation(locX, locY); d.setVisible(true); }
	 * 
	 * frame.setJMenuBar(menuBar); frame.revalidate(); }
	 * 
	 * this.frame = frame; }
	 * 
	 *//**
		 * Adds a pane to the side of the main window.
		 * 
		 * @param pane
		 *            A JComponent containing the UI for the given side
		 * @param side
		 *            A side, as given by BorderLayout.[EAST][WEST][...]
		 */
	/*
	 * protected void addPane(JComponent pane, String side) { if (frame != null)
	 * { JComponent old = sidePanes.get(side); if (old != null) {
	 * frame.remove(old); } frame.add(pane, side);
	 * 
	 * frame.revalidate(); }
	 * 
	 * sidePanes.put(side, pane); }
	 * 
	 *//**
		 * Sets the main window's menu bar.
		 * 
		 * @param menuBar
		 */
	/*
	 * protected void setMainJMenuBar(JMenuBar menuBar) { if(frame != null) {
	 * frame.setJMenuBar(menuBar); frame.revalidate(); } for(int i = 0; i <
	 * menuBar.getMenuCount(); i++) { // forces the menu to work correctly on
	 * top of the LWGJL view
	 * menuBar.getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false); }
	 * this.menuBar = menuBar; }
	 * 
	 * 
	 *//**
		 * The opposite of addPane.
		 * 
		 * @param pane
		 *            The pane to remove
		 */
	/*
	 * protected void removePane(JComponent pane) { if (frame != null) { for
	 * (Component c : frame.getComponents()) { System.out.println(c); }
	 * frame.remove(pane); frame.revalidate(); }
	 * sidePanes.values().remove(pane); }
	 * 
	 *//**
		 * The opposite of addPane.
		 * 
		 * @param side
		 *            The side that contains a pane to remove
		 */
	/*
	 * protected void removePane(String side) { JComponent pane =
	 * sidePanes.remove(side);
	 * 
	 * if (pane != null) { if (frame != null) { frame.remove(pane);
	 * frame.revalidate(); } } }
	 * 
	 *//**
		 * Adds an overlay window for this screen.
		 * 
		 * @param c
		 *            A component containing the desired content
		 * @param title
		 *            The title of the frame
		 */
	/*
	 * protected void addWindowFor(JComponent c, String title) { addWindowFor(c,
	 * title, null); }
	 * 
	 * 
	 *//**
		 * Adds an overlay window for this screen.
		 * 
		 * @param c
		 *            A component containing the desired content
		 * @param title
		 *            The title of this new window
		 * @param menu
		 *            A MenuBar to put onto this new window
		 */
	/*
	 * protected void addWindowFor(JComponent c, String title, JMenuBar menu) {
	 * overlayWindowComponentsNames.put(c, title); otherWindowMenus.put(c,
	 * menu);
	 * 
	 * if(frame != null) { JDialog d = new JDialog(frame, title);
	 * d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); d.add(c);
	 * d.setJMenuBar(menu); d.pack(); int locX = frame.getLocationOnScreen().x +
	 * frame.getWidth()/2 - d.getWidth()/2; int locY =
	 * frame.getLocationOnScreen(). y + frame.getHeight()/2 - d.getHeight()/2;
	 * d.setLocation(locX, locY); d.setVisible(true); } }
	 * 
	 * 
	 *//**
		 * Attaches (or detaches) a MenuBar to the window for a given component.
		 * 
		 * @param c
		 *            The component that was previously added to this using
		 *            addWindowFor()
		 * @param menu
		 *            The menu to attach to the given window
		 *//*
		 * protected void setWindowJMenuBar(JComponent c, JMenuBar menu) {
		 * otherWindowMenus.put(c, menu);
		 * 
		 * if(frame != null) { try { JDialog d = (JDialog) c.getParent();
		 * d.setJMenuBar(menu); } catch(ClassCastException ex) { // TODO }
		 * 
		 * } }
		 */
}
