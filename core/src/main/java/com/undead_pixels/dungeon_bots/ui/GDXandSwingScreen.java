package com.undead_pixels.dungeon_bots.ui;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;
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
	private HashSet<JInternalFrame> internalFrames = new HashSet<>();
	
	public final void attachToFrame(JFrame frame) {
		
		if(frame == null) {
			for(String s : sidePanes.keySet()) {
				this.frame.remove(sidePanes.get(s));
			}
			
			for(JInternalFrame f : internalFrames) {
				this.frame.getLayeredPane().remove(f);
				f.setVisible(false);
			}
			
			this.frame.setJMenuBar(null);
			this.frame.revalidate();
		} else {
			for(String s : sidePanes.keySet()) {
				frame.add(sidePanes.get(s), s);
			}

			for(JInternalFrame f : internalFrames) {
				this.frame.getLayeredPane().add(f, new Integer(10));
				f.setVisible(true);
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
	public void setJMenuBar(JMenuBar menuBar) {
		if(frame != null) {
			frame.setJMenuBar(menuBar);
			frame.revalidate();
		}
		this.menuBar = menuBar;
	}

	public void removePane(JComponent pane) {
		if(frame != null) {
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
	
	public void addInternalFrame(JInternalFrame f) {
		internalFrames.add(f);
		
		if(frame != null) {
			frame.getLayeredPane().add(f, new Integer(10));
			f.setVisible(true);
			frame.revalidate();
		}
	}
}
