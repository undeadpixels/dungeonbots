package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import org.jdesktop.swingx.JXCollapsiblePane;

import com.undead_pixels.dungeon_bots.libraries.StretchIcon;

public class UIBuilder {

	public UIBuilder() {
		// TODO Auto-generated constructor stub
	}

	public static boolean verbose = true;

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
	 * @param imageURL
	 *            The image to put on the toggle button. If given null or "",
	 *            the toggle button will contain the altText.
	 * 
	 * @param toolTipText
	 *            The tool tip to display when the mouse hovers over the button.
	 *            If given null or "", no tool tip text will be set.
	 * @param actionCommand
	 *            The String action command this button will issue to any
	 *            listeners. If given null or "", no action command will be set.
	 * @param listener
	 *            The ActionListener to receive commands from this button. If
	 *            null is given, no listener will be added.
	 */
	public static JButton makeButton(String imageURL, String toolTipText, String actionCommand,
			ActionListener listener) {

		JButton resultButton = new JButton();
		if (actionCommand != null && !actionCommand.equals(""))
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
		Image img = UIBuilder.getImage(imageURL);
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
	 * button is in a focused window. *
	 * 
	 * @param imageURL
	 *            The image to put on the toggle button. If given null or "",
	 *            the toggle button will contain the altText.
	 * 
	 * @param toolTipText
	 *            The tool tip to display when the mouse hovers over the button.
	 *            If given null or "", no tool tip text will be set.
	 * @param altText
	 *            The text to place in the button, if the given imageURL cannot
	 *            be found.
	 * @param actionCommand
	 *            The String action command this button will issue to any
	 *            listeners. If given null or "", no action command will be set.
	 * @param listener
	 *            The ActionListener to receive commands from this button. If
	 *            null is given, no listener will be added.
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

	/**
	 * Makes and returns a toggle button.
	 * 
	 * @param imageURL
	 *            The image to put on the toggle button. If given null or "",
	 *            the toggle button will contain the altText.
	 * @param toolTipText
	 *            The tool tip to display when the mouse hovers over the button.
	 *            If given null or "", no tool tip text will be set.
	 * @param altText
	 *            The text to place in the button, if the given imageURL cannot
	 *            be found.
	 * @param actionCommand
	 *            The String action command this button will issue to any
	 *            listeners. If given null or "", no action command will be set.
	 * @param listener
	 *            The ActionListener to receive commands from this button. If
	 *            null is given, no listener will be added.
	 */

	public static JToggleButton makeToggleButton(String imageURL, String toolTipText, String altText,
			String actionCommand, ActionListener listener) {

		JToggleButton resultButton = new JToggleButton();
		if (actionCommand != null && !actionCommand.equals(""))
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
		Image img = UIBuilder.getImage(imageURL);
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

	/**
	 * Makes a collapser with a toggling header line.
	 * 
	 * @param content
	 *            The content of the collapse while expanded.
	 * @param closedText
	 *            The header text while the collapser is closed.
	 * @param openText
	 *            The header text while the collapser is open.
	 * @param toolTip
	 *            The tooltip while the mouse hovers over the collapsing toggle.
	 * @param isCollapsed
	 *            The status of the collapser initially.
	 */
	public static JPanel makeCollapser(Container content, String closedText, String openText, String toolTip,
			boolean isCollapsed) {

		JXCollapsiblePane collapser = new JXCollapsiblePane();
		collapser.setContentPane(content);
		collapser.setCollapsed(isCollapsed);
		//collapser.setPreferredSize(new Dimension(100, 400));

		JLabel lbl = new JLabel();
		lbl.setText(isCollapsed ? closedText : openText);
		JToggleButton toggler = makeToggleButton("", toolTip, "", "TOGGLE", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				if (!arg0.getActionCommand().equals("TOGGLE"))
					return;
				collapser.setCollapsed(!collapser.isCollapsed());
				lbl.setText(collapser.isCollapsed() ? closedText : openText);
				((JToggleButton) arg0.getSource()).setText(collapser.isCollapsed() ? ">" : "v");
			}
		});
		toggler.setPreferredSize(new Dimension(40, 30));
		toggler.setText(isCollapsed ? ">" : "v");
		JPanel header = new JPanel();
		header.setLayout(new FlowLayout());
		header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		header.add(toggler);
		header.add(lbl);

		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		result.add(header, BorderLayout.PAGE_START);
		result.add(collapser, BorderLayout.CENTER);
		/*result.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (e.getID() != ComponentEvent.COMPONENT_RESIZED)
					return;
				//int height = result.getHeight() - header.getHeight();
				collapser.setPreferredSize(new Dimension(result.getWidth(), 400));
				//System.out.println(e.getID() + " " + result.getSize().toString());
			}
		});*/

		return result;

	}

	/** The cached GUI images. */
	private static HashMap<String, Image> _Images = new HashMap<String, Image>();

	/**
	 * Gets an Image based on the image at the given location. Also, caches
	 * loaded images so that a call to the same image resource will not load it
	 * twice. If no image exists at the given location, returns null and prints
	 * a missing resource message to System.err.
	 */
	public static Image getImage(String filename) {
		if (filename == null || filename.equals(""))
			return null;

		if (_Images.containsKey(filename))
			return _Images.get(filename);

		String path = System.getProperty("user.dir") + "/images/" + filename;
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException ioex) {
			if (verbose)
				System.err.println("Image resource missing: " + path);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		_Images.put(filename, img);

		return img;
	}

}
