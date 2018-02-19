package com.undead_pixels.dungeon_bots.utils.builders;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.libraries.StretchIcon;

public class UIBuilder {

	public UIBuilder() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Generates a new button that uses the given image, size, tooltip text, and
	 * alternative text. The given action command will be sent to the specified
	 * action listener.
	 * 
	 * If the given image URL is not available, the alternative text will be
	 * used for the button's image.
	 */
	public static JButton makeButton(String imageURL, int width, int height, String toolTipText, String actionCommand,
			ActionListener listener) {
		JButton ret = makeButton(imageURL, toolTipText, actionCommand, listener);
		ret.setPreferredSize(new Dimension(width, height));
		return ret;
	}

	/**
	 * Generates a new button that uses the given image, tooltip text, and
	 * alternative text. The given action command will be sent to the specified
	 * action listener.
	 * 
	 * If the given image URL is not available, the alternative text will be
	 * used for the button's image.
	 */
	public static JButton makeButton(String imageURL, String toolTipText, String actionCommand,
			ActionListener listener) {

		JButton resultButton = new JButton();
		resultButton.setActionCommand(actionCommand);
		if (toolTipText != null && !toolTipText.equals(""))
			resultButton.setToolTipText(toolTipText);

		// Wire up the listener, and make the button respond as clicked when it
		// has focus and enter is pressed.
		if (listener != null)
			resultButton.addActionListener(listener);
		resultButton.registerKeyboardAction(
				resultButton.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);
		resultButton.registerKeyboardAction(
				resultButton.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);

		// Set the image if the resource can be found. If it can't set the
		// contents of the button to the alternative text.
		Image img = DungeonBotsMain.getImage(imageURL);
		if (img == null)
			resultButton.setText(actionCommand);
		else
			// Proportionate probably makes sense most times.
			resultButton.setIcon(new StretchIcon(img, true));

		resultButton.setPreferredSize(new Dimension(50, 40));
		return resultButton;
	}

	/**
	 * Create a button with an associated hot key that will apply so long as the
	 * button is in a focused window.
	 */
	public static JButton makeButton(String imageURL, String toolTipText, String actionCommand, ActionListener listener,
			KeyStroke hotKey) {

		JButton resultButton = makeButton(imageURL, toolTipText, actionCommand, listener);

		resultButton.registerKeyboardAction(
				resultButton.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
				KeyStroke.getKeyStroke(hotKey.getKeyCode(), hotKey.getModifiers(), false),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		resultButton.registerKeyboardAction(
				resultButton.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
				KeyStroke.getKeyStroke(hotKey.getKeyCode(), hotKey.getModifiers(), true),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		return resultButton;

	}

	public static JToggleButton makeToggleButton(String imageURL, String toolTipText, String altText,
			String actionCommand, ActionListener listener) {

		JToggleButton resultButton = new JToggleButton();
		resultButton.setActionCommand(actionCommand);
		if (toolTipText != null && !toolTipText.equals(""))
			resultButton.setToolTipText(toolTipText);

		// Wire up the listener, and make the button respond as clicked when it
		// has focus and enter is pressed.
		if (listener != null)
			resultButton.addActionListener(listener);
		resultButton.registerKeyboardAction(
				resultButton.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);
		resultButton.registerKeyboardAction(
				resultButton.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);

		// Set the image if the resource can be found. If it can't set the
		// contents of the button to the alternative text.
		Image img = DungeonBotsMain.getImage(imageURL);
		if (img == null)
			resultButton.setText(altText);
		else
			// Proportionate probably makes sense most times.
			resultButton.setIcon(new StretchIcon(img, true));

		resultButton.setPreferredSize(new Dimension(50, 40));

		return resultButton;
	}

	/**
	 * Creates a menu item with the given header, that responds to the given
	 * accelerator and mnemonic. The accelerator is a key chord that will invoke
	 * the item even if the menu is not open. The mnemonic is the underlined
	 * letter of a menu item.
	 */
	public static JMenuItem makeMenuItem(String header, KeyStroke accelerator, int mnemonic, ActionListener listener) {
		JMenuItem result = new JMenuItem(header);

		if (accelerator != null) {
			result.setAccelerator(accelerator);
		}

		if (mnemonic != 0)
			result.setMnemonic(mnemonic);

		result.setActionCommand(header);
		result.addActionListener(listener);

		return result;

	}
}
