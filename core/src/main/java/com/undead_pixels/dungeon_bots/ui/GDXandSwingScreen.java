package com.undead_pixels.dungeon_bots.ui;

import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;

import com.badlogic.gdx.Screen;

public class GDXandSwingScreen implements Screen {

	@Override
	public void show() {

	}

	@Override
	public void render(float delta) {

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {

	}

	
	private JFrame frame;
	
	private JMenuBar menuBar;
	private HashMap<String, JComponent> sidePanes = new HashMap<>();
	private HashMap<JComponent, String> otherWindowComponents = new HashMap<>();
	private HashMap<JComponent, JMenuBar> otherWindowMenus = new HashMap<>();
	
	public final void attachScreenToFrame(JFrame frame) {
		
		if(frame == null) {
			for(String s : sidePanes.keySet()) {
				this.frame.remove(sidePanes.get(s));
			}
			
			for(JComponent c : otherWindowComponents.keySet()) {
				Container p = c.getParent();
				if(p != null) {
					p.setVisible(false);
					p.remove(c);
				}
			}
			
			this.frame.setJMenuBar(null);
			this.frame.revalidate();
		} else {
			for(String s : sidePanes.keySet()) {
				frame.add(sidePanes.get(s), s);
			}

			for(JComponent c : otherWindowComponents.keySet()) {
				String title = otherWindowComponents.get(c);
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
	 * Sets the window's menu bar
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
	public void removePane(String side) {
		JComponent pane = sidePanes.remove(side);
		
		if(pane != null) {
			if(frame != null) {
				frame.remove(pane);
				frame.revalidate();
			}
		}
	}

	public void addWindowFor(JComponent c, String title) {
		addWindowFor(c, title, null);
	}
	public void addWindowFor(JComponent c, String title, JMenuBar menu) {
		otherWindowComponents.put(c, title);
		otherWindowMenus.put(c, menu);
		
		if(frame != null) {
			JDialog d = new JDialog(frame, title);
			d.add(c);
			d.setJMenuBar(menu);
			d.pack();
			int locX = frame.getLocationOnScreen().x + frame.getWidth()/2 - d.getWidth()/2;
			int locY = frame.getLocationOnScreen(). y + frame.getHeight()/2 - d.getHeight()/2;
			d.setLocation(locX, locY);
			d.setVisible(true);
		}
	}
	

	public void setWindowJMenuBar(JComponent c, JMenuBar menu) {
		
	}
}
