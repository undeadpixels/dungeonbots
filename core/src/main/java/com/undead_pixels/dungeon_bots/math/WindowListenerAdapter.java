package com.undead_pixels.dungeon_bots.math;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * The purpose of this class is entirely to make the WindowListener more
 * expressive, since it is often the case that only a single method is handled
 * in an implemented WindowListener. Just test the event argument for the event
 * code, and response accordingly.
 */
public abstract class WindowListenerAdapter implements WindowListener {

	/**
	 * Non-specific event method, designed to make controllers a little more
	 * expressive.
	 */
	protected abstract void event(WindowEvent e);

	@Override
	public void windowActivated(WindowEvent e) {
		event(e);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		event(e);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		event(e);
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		event(e);
	}

	@Override
	public void windowIconified(WindowEvent e) {
		event(e);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		event(e);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		event(e);
	}

}
