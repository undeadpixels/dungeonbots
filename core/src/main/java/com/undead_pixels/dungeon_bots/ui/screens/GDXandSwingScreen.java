package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * An upgraded version of a GDX Screen that also allows javax.swing widgets to be plopped
 * alongside or in overlaying windows.
 */
public class GDXandSwingScreen {
	
	/**
	 * Internal reference to the frame the GDX's context (and any side panels this owns) lives in.
	 */
	private JFrame frame;
	
	/**
	 * The top menu bar for File, Edit, ... (if any).
	 */
	private JMenuBar menuBar;
	
	/**
	 * A map of which sides various panes are attached where on the main window.
	 */
	private HashMap<String, JComponent> sidePanes = new HashMap<>();
	
	/**
	 * A map of names for overlaying windows vs their contents.
	 * The windows are stored implicitly in component.getParent().
	 */
	private HashMap<JComponent, String> overlayWindowComponentsNames = new HashMap<>();
	
	/**
	 * A map of menus for each of the overlaying windows.
	 */
	private HashMap<JComponent, JMenuBar> otherWindowMenus = new HashMap<>();
	
	/**
	 * This should only be called by DungeonBotsMain.
	 * 
	 * It just signals that this screen is either becoming active
	 * (and being attached to the given JFrame) or is becoming inactive
	 * and should remove any children from the JFrame it was previously
	 * attached to.
	 * 
	 * @param frame
	 */
	public final void attachScreenToFrame(JFrame frame) {
		if(frame == null) {
			// Detach any children from the old frame
			for(String s : sidePanes.keySet()) {
				this.frame.remove(sidePanes.get(s));
			}
			
			// Destroy any child windows
			for(JComponent c : overlayWindowComponentsNames.keySet()) {
				Container p = c.getParent();
				if(p != null) {
					p.setVisible(false);
					p.remove(c);
				}
			}
			
			this.frame.setJMenuBar(null);
			this.frame.revalidate();
		} else {
			// Attach any children to the new frame
			for(String s : sidePanes.keySet()) {
				frame.add(sidePanes.get(s), s);
			}

			// Create/show any child windows
			for(JComponent c : overlayWindowComponentsNames.keySet()) {
				String title = overlayWindowComponentsNames.get(c);
				JMenuBar menu = otherWindowMenus.get(c);
				JDialog d = new JDialog(frame, title);
				d.add(c);
				d.setJMenuBar(menu);
				d.pack();
				int locX = frame.getLocationOnScreen().x + frame.getWidth()/2 - d.getWidth()/2;
				int locY = frame.getLocationOnScreen(). y + frame.getHeight()/2 - d.getHeight()/2;
				d.setLocation(locX, locY);
				d.setVisible(true);
			}
			
			frame.setJMenuBar(menuBar);
			frame.revalidate();
		}

		this.frame = frame;
	}

	/**
	 * Adds a pane to the side of the main window.
	 * 
	 * @param pane	A JComponent containing the UI for the given side
	 * @param side	A side, as given by BorderLayout.[EAST][WEST][...]
	 */
	public void addPane(JComponent pane, String side) {
		if(frame != null) {
			JComponent old = sidePanes.get(side);
			if(old != null) {
				frame.remove(old);
			}
			frame.add(pane, side);
			
			frame.revalidate();
		}

		sidePanes.put(side, pane);
	}
	
	/**
	 * Sets the main window's menu bar.
	 * 
	 * @param menuBar
	 */
	public void setMainJMenuBar(JMenuBar menuBar) {
		if(frame != null) {
			frame.setJMenuBar(menuBar);
			frame.revalidate();
		}
		for(int i = 0; i < menuBar.getMenuCount(); i++) {
			// forces the menu to work correctly on top of the LWGJL view
			menuBar.getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false);
		}
		this.menuBar = menuBar;
	}

	/**
	 * The opposite of addPane.
	 * 
	 * @param pane	The pane to remove
	 */
	public void removePane(JComponent pane) {
		if(frame != null) {
			for(Component c : frame.getComponents()) {
				System.out.println(c);
			}
			frame.remove(pane);
			frame.revalidate();
		}
		sidePanes.values().remove(pane);
	}
	
	/**
	 * The opposite of addPane.
	 * 
	 * @param side	The side that contains a pane to remove
	 */
	public void removePane(String side) {
		JComponent pane = sidePanes.remove(side);
		
		if(pane != null) {
			if(frame != null) {
				frame.remove(pane);
				frame.revalidate();
			}
		}
	}

	/**
	 * Adds an overlay window for this screen.
	 * 
	 * @param c		A component containing the desired content
	 * @param title	The title of the frame
	 */
	public void addWindowFor(JComponent c, String title) {
		addWindowFor(c, title, null);
	}
	
	/**
	 * Adds an overlay window for this screen.
	 * 
	 * @param c		A component containing the desired content
	 * @param title	The title of this new window
	 * @param menu	A MenuBar to put onto this new window
	 */
	public void addWindowFor(JComponent c, String title, JMenuBar menu) {
		overlayWindowComponentsNames.put(c, title);
		otherWindowMenus.put(c, menu);
		
		if(frame != null) {
			JDialog d = new JDialog(frame, title);
			d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			d.add(c);
			d.setJMenuBar(menu);
			d.pack();
			int locX = frame.getLocationOnScreen().x + frame.getWidth()/2 - d.getWidth()/2;
			int locY = frame.getLocationOnScreen(). y + frame.getHeight()/2 - d.getHeight()/2;
			d.setLocation(locX, locY);
			d.setVisible(true);
		}
	}
	

	/**
	 * Attaches (or detaches) a MenuBar to the window for a given component.
	 * 
	 * @param c		The component that was previously added to this using addWindowFor()
	 * @param menu	The menu to attach to the given window
	 */
	public void setWindowJMenuBar(JComponent c, JMenuBar menu) {
		otherWindowMenus.put(c, menu);
		
		if(frame != null) {
			try {
				JDialog d = (JDialog) c.getParent();
				d.setJMenuBar(menu);
			} catch(ClassCastException ex) {
				// TODO
			}
			
		}
	}
}
