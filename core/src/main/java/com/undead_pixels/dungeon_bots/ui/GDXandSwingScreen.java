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
	private JComponent rootPanel;
	
	private JMenuBar menuBar;
	private HashMap<String, JComponent> sidePanes = new HashMap<>();
	private HashMap<JComponent, String> otherWindowComponents = new HashMap<>();
	
	public final void attachToFrame(JFrame frame, JComponent rootPanel) {
		
		if(frame == null) {
			for(String s : sidePanes.keySet()) {
				this.rootPanel.remove(sidePanes.get(s));
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
				rootPanel.add(sidePanes.get(s), s);
			}

			for(JComponent c : otherWindowComponents.keySet()) {
				JDialog d = new JDialog(frame);
				d.add(c);
				d.pack();
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
		if(rootPanel != null) {
			JComponent old = sidePanes.get(side);
			if(old != null) {
				rootPanel.remove(old);
			}
			rootPanel.add(pane, side);
			
			frame.revalidate();
		}

		sidePanes.put(side, pane);
	}
	
	/**
	 * Sets the window's menu bar
	 * 
	 * @param menuBar
	 */
	public void setJMenuBar(JMenuBar menuBar) {
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
			for(Component c : rootPanel.getComponents()) {
				System.out.println(c);
			}
			rootPanel.remove(pane);
			frame.revalidate();
		}
		sidePanes.values().remove(pane);
	}
	public void removePane(String side) {
		JComponent pane = sidePanes.remove(side);
		
		if(pane != null) {
			if(frame != null) {
				rootPanel.remove(pane);
				frame.revalidate();
			}
		}
	}
	
	public void addWindowFor(JComponent c, String title) {
		otherWindowComponents.put(c, title);
		
		if(frame != null) {
			JDialog d = new JDialog(frame, title);
			d.add(c);
			d.pack();
			d.setLocation(frame.getLocationOnScreen().x + frame.getWidth()/2, frame.getLocationOnScreen(). y + frame.getHeight()/2);
			d.setVisible(true);
		}
	}
}
