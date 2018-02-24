package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;

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

		return buildButton().image(imageURL).width(width).height(height).toolTip(toolTipText)
				.action(actionCommand, listener).create();
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
		return buildButton().image(imageURL).toolTip(toolTipText).action(actionCommand, listener).create();
	}

	public static abstract class ButtonBuilder<T extends AbstractButton> {

		protected static final String FIELD_ACTION = "action";
		protected static final String FIELD_ACTION_COMMAND = "action_command";
		protected static final String FIELD_ACTION_COMMAND_LISTENER = "action_command_listener";
		protected static final String FIELD_ALIGNMENT_X = "alignment_x";
		protected static final String FIELD_ALIGNMENT_Y = "alignment_x";
		protected static final String FIELD_FOCUSABLE = "focusable";
		protected static final String FIELD_HOTKEY = "hotkey";
		protected static final String FIELD_IMAGE = "image";
		protected static final String FIELD_INSETS = "insets";
		protected static final String FIELD_PREFERRED_WIDTH = "preferred_width";
		protected static final String FIELD_PREFERRED_HEIGHT = "preferred_height";
		protected static final String FIELD_TEXT = "text";
		protected static final String FIELD_TOOLTIP = "tooltip";

		protected static final int DEFAULT_PREFERRED_WIDTH = 50;
		protected static final int DEFAULT_PREFERRED_HEIGHT = 35;

		private HashMap<String, Object> _Settings = new HashMap<String, Object>();

		/** Clears all settings from this ButtonBuilder. */
		public final void reset() {
			_Settings.clear();
		}

		/** Sets the given action for buttons created by this builder. */
		public final ButtonBuilder<T> action(Action action) {
			_Settings.put(FIELD_ACTION, action);
			return this;
		}

		/**
		 * Specifies the action command, and adds the given listener to buttons
		 * created by this builder.
		 */
		public final ButtonBuilder<T> action(String command, ActionListener listener) {
			_Settings.put(FIELD_ACTION_COMMAND, command);
			_Settings.put(FIELD_ACTION_COMMAND_LISTENER, listener);
			return this;
		}

		public final ButtonBuilder<T> alignmentX(float alignment) {
			_Settings.put(FIELD_ALIGNMENT_X, alignment);
			return this;
		}

		public final ButtonBuilder<T> alignmentY(float alignment) {
			_Settings.put(FIELD_ALIGNMENT_Y, alignment);
			return this;
		}

		/** Sets focusability as specified. */
		public final ButtonBuilder<T> focusable(boolean focusable) {
			_Settings.put(FIELD_FOCUSABLE, focusable);
			return this;
		}

		/**
		 * Specifies the preferred height for buttons created by this builder.
		 */
		public final ButtonBuilder<T> height(int height) {
			_Settings.put(FIELD_PREFERRED_HEIGHT, height);
			return this;
		}

		/**
		 * Specifies the hotkey for buttons created by this builder (note that
		 * this may result in multiple buttons having the same hotkey).
		 */
		public final ButtonBuilder<T> hotkey(char keyChar, boolean shift, boolean ctrl, boolean alt, boolean meta,
				boolean alt_graph) {
			int mod = 0;
			if (shift)
				mod |= InputEvent.SHIFT_DOWN_MASK;
			if (ctrl)
				mod |= InputEvent.CTRL_DOWN_MASK;
			if (alt)
				mod |= InputEvent.ALT_DOWN_MASK;
			if (meta)
				mod |= InputEvent.META_DOWN_MASK;
			if (alt_graph)
				mod |= InputEvent.ALT_GRAPH_DOWN_MASK;
			return hotkey(keyChar, mod);
		}

		/**
		 * Specifies the hotkey for buttons created by this builder (note that
		 * this may result in multiple buttons having the same hotkey).
		 */
		public final ButtonBuilder<T> hotkey(char keyChar) {
			return hotkey(keyChar, 0);
		}

		/**
		 * Specifies the hotkey for buttons created by this builder (note that
		 * this may result in multiple buttons having the same hotkey).
		 */
		public final ButtonBuilder<T> hotkey(char keyChar, int modifiers) {
			return hotkey(KeyStroke.getKeyStroke(new Character(keyChar), modifiers));
		}

		/**
		 * Specifies the hotkey for buttons created by this builder (note that
		 * this may result in multiple buttons having the same hotkey).
		 */
		public final ButtonBuilder<T> hotkey(KeyStroke hotKey) {
			_Settings.put(FIELD_HOTKEY, hotKey);
			return this;
		}

		/**
		 * A class whose only purpose is to pair an image with its intended
		 * proportionality flag.
		 */
		private static final class ImageProportionality {
			public Image image;
			public boolean proportional;

			public ImageProportionality(Image image, boolean proportional) {
				this.image = image;
				this.proportional = proportional;
			}
		}

		/**
		 * Specifies an image from the given filename will be displayed by
		 * buttons created by this builder. If the file could not be opened,
		 * sets the displayed text to the filename.
		 */
		public final ButtonBuilder<T> image(String filename) {
			_Settings.put(FIELD_IMAGE, filename);
			return this;
		}

		/**
		 * Specifies an image to be displayed by this button. The image will
		 * fill the entire button.
		 */
		public final ButtonBuilder<T> image(Image image) {
			return image(image, false);
		}

		/**
		 * Specifies an image to be displayed by this button. If the given
		 * boolean is true, the image will maintain proportionality within the
		 * button but be scaled as large as it can but still fit. If false, the
		 * image will be stretched to completely fill the button.
		 */
		public final ButtonBuilder<T> image(Image image, boolean proportional) {
			_Settings.put(FIELD_IMAGE, new ImageProportionality(image, proportional));
			return this;
		}

		/** Sets the button's margin as indicated. */
		public final ButtonBuilder<T> margin(int top, int left, int bottom, int right) {
			return margin(new Insets(top, left, bottom, right));
		}

		/** Sets the button's margin as indicated. */
		public final ButtonBuilder<T> margin(Insets insets) {
			_Settings.put(FIELD_INSETS, insets);
			return this;
		}

		/**
		 * Specifies the given text to be displayed by buttons created by this
		 * builder.
		 */
		public final ButtonBuilder<T> text(String text) {
			_Settings.put(FIELD_TEXT, text);
			return this;
		}

		/** Adds the given tooltip text to buttons created by this builder. */
		public final ButtonBuilder<T> toolTip(String toolTipText) {
			_Settings.put(FIELD_TOOLTIP, toolTipText);
			return this;
		}

		/**
		 * Specifies the preferred width for buttons created by this builder.
		 */
		public final ButtonBuilder<T> width(int width) {
			_Settings.put(FIELD_PREFERRED_WIDTH, width);
			return this;
		}

		protected abstract void addSettings(T buttonBeingBuilt, HashMap<String, Object> unhandledSettings);

		/**
		 * Create an uninitialized button as it is defined in Swing, for example
		 * a JButton or a JToggleButton. Settings will be applied after the
		 * button is instantiated.
		 */
		protected abstract T createUninitialized();

		public T create() {

			// Create the base object.
			T bttn = createUninitialized();

			// Create a copy of the settings map.
			HashMap<String, Object> unhandled = new HashMap<String, Object>();
			for (Entry<String, Object> entry : this._Settings.entrySet())
				unhandled.put(entry.getKey(), entry.getValue());

			// Check each possible setting for an AbstractButton.
			if (unhandled.containsKey(FIELD_ACTION))
				bttn.setAction((Action) unhandled.remove(FIELD_ACTION));
			if (unhandled.containsKey(FIELD_ACTION_COMMAND))
				bttn.setActionCommand((String) unhandled.remove(FIELD_ACTION_COMMAND));
			if (unhandled.containsKey(FIELD_ACTION_COMMAND_LISTENER))
				bttn.addActionListener((ActionListener) unhandled.remove(FIELD_ACTION_COMMAND_LISTENER));
			if (unhandled.containsKey(FIELD_ALIGNMENT_X))
				bttn.setAlignmentX((float) unhandled.remove(FIELD_ALIGNMENT_X));
			if (unhandled.containsKey(FIELD_ALIGNMENT_Y))
				bttn.setAlignmentX((float) unhandled.remove(FIELD_ALIGNMENT_Y));
			if (unhandled.containsKey(FIELD_FOCUSABLE))
				bttn.setFocusable((boolean) unhandled.remove(FIELD_FOCUSABLE));
			if (unhandled.containsKey(FIELD_TOOLTIP))
				bttn.setToolTipText((String) unhandled.remove(FIELD_TOOLTIP));
			if (unhandled.containsKey(FIELD_IMAGE)) {
				Object value = unhandled.remove(FIELD_IMAGE);
				ImageProportionality ip = null;
				if (value instanceof String) {
					ip = new ImageProportionality(UIBuilder.getImage((String) value), false);
					if (ip.image == null)
						bttn.setText((String) value);
				} else if (value instanceof ImageProportionality)
					ip = (ImageProportionality) value;
				if (ip.image != null)
					bttn.setIcon(new StretchIcon(ip.image, ip.proportional));
			}
			if (unhandled.containsKey(FIELD_INSETS))
				bttn.setMargin((Insets) unhandled.remove(FIELD_INSETS));
			if (unhandled.containsKey(FIELD_PREFERRED_WIDTH) || unhandled.containsKey(FIELD_PREFERRED_HEIGHT)) {
				int prefWidth = unhandled.containsKey(FIELD_PREFERRED_WIDTH)
						? (int) unhandled.remove(FIELD_PREFERRED_WIDTH) : DEFAULT_PREFERRED_WIDTH;
				int prefHeight = unhandled.containsKey(FIELD_PREFERRED_HEIGHT)
						? (int) unhandled.remove(FIELD_PREFERRED_HEIGHT) : DEFAULT_PREFERRED_HEIGHT;
				bttn.setPreferredSize(new Dimension(prefWidth, prefHeight));
			}
			if (unhandled.containsKey(FIELD_HOTKEY)) {
				KeyStroke hotkey = (KeyStroke) unhandled.remove(FIELD_HOTKEY);
				bttn.registerKeyboardAction(
						bttn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
						KeyStroke.getKeyStroke(hotkey.getKeyCode(), hotkey.getModifiers(), false),
						JComponent.WHEN_IN_FOCUSED_WINDOW);
				bttn.registerKeyboardAction(
						bttn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
						KeyStroke.getKeyStroke(hotkey.getKeyCode(), hotkey.getModifiers(), true),
						JComponent.WHEN_IN_FOCUSED_WINDOW);
			}
			if (unhandled.containsKey(FIELD_TEXT)) {
				bttn.setText((String) unhandled.remove(FIELD_TEXT));
			}

			if (unhandled.size() > 0)
				addSettings(bttn, unhandled);

			// The 'enter' key should do the same as the 'space' key.
			bttn.registerKeyboardAction(bttn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);
			bttn.registerKeyboardAction(bttn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);

			return bttn;
		}

	}

	/**
	 * Returns a builder for a JButton. The builder pattern will allow the
	 * following call:
	 * <p>
	 * JButton button = buildButton().text("I'm a button").action("CLICK",
	 * listener).create();
	 */
	public static ButtonBuilder<JButton> buildButton() {
		return new ButtonBuilder<JButton>() {

			@Override
			protected void addSettings(JButton buttonBeingBuilt, HashMap<String, Object> unhandledSettings) {
				throw new RuntimeException("Have not implemented settings: " + unhandledSettings.toString());
			}

			@Override
			protected JButton createUninitialized() {
				return new JButton();
			}
		};
	}

	public static ButtonBuilder<JToggleButton> buildToggleButton() {
		return new ButtonBuilder<JToggleButton>() {

			@Override
			protected void addSettings(JToggleButton buttonBeingBuilt, HashMap<String, Object> unhandledSettings) {
				throw new RuntimeException("Have not implemented settings: " + unhandledSettings.toString());
			}

			@Override
			protected JToggleButton createUninitialized() {
				return new JToggleButton();
			}

		};
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
	/*
	 * public static JButton makeButton(String imageURL, String toolTipText,
	 * String actionCommand, ActionListener listener, KeyStroke hotKey) {
	 * 
	 * return
	 * buildButton().image(imageURL).toolTip(toolTipText).action(actionCommand,
	 * listener).hotkey(hotKey) .create(); }
	 */

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
		return buildToggleButton().image(imageURL).toolTip(toolTipText).action(actionCommand, listener).create();
	}

	/**
	 * Creates a menu item with the given header, that responds to the given
	 * accelerator and mnemonic. The accelerator is a key chord that will invoke
	 * the item even if the menu is not open. The mnemonic is the underlined
	 * letter of a menu item. The command conveyed to the listener will be the
	 * header.
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
	 * Creates a menu item with the given header. The command conveyed to the
	 * listener will be the header.
	 */
	public static JMenuItem makeMenuItem(String header, ActionListener listener) {
		return makeMenuItem(header, null, 0, listener);
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
		// collapser.setPreferredSize(new Dimension(100, 400));

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
		/*
		 * result.addComponentListener(new ComponentAdapter() {
		 * 
		 * @Override public void componentResized(ComponentEvent e) { if
		 * (e.getID() != ComponentEvent.COMPONENT_RESIZED) return; //int height
		 * = result.getHeight() - header.getHeight();
		 * collapser.setPreferredSize(new Dimension(result.getWidth(), 400));
		 * //System.out.println(e.getID() + " " + result.getSize().toString());
		 * } });
		 */

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

	/**
	 * Returns a login that uses the given password store and username store,
	 * and uses the specified listener for login events.
	 * 
	 * @param passwordStore
	 *            A password store uses the last successful login password to
	 *            pre-populate the password field. If null, none will be set.
	 * @param userNameStore
	 *            A username store uses the last successful login username to
	 *            pre-populate the username field. If null, none will be set.
	 * @param loginService
	 *            The login service used to login.
	 */
	public static JXLoginPane makeLogin(String message, PasswordStore passwordStore, UserNameStore userNameStore,
			LoginService loginService) {
		JXLoginPane pane = new JXLoginPane(loginService);
		if (passwordStore != null)
			pane.setPasswordStore(passwordStore);
		if (userNameStore != null)
			pane.setUserNameStore(userNameStore);
		pane.setBannerText(message);
		return pane;
	}

	/**
	 * Never roll your own security. This method blocks and returns a status
	 * regarding the login.
	 * 
	 * @param passwordStore
	 *            A password store uses the last successful login password to
	 *            pre-populate the password field. If null, none will be set.
	 * @param userNameStore
	 *            A username store uses the last successful login username to
	 *            pre-populate the username field. If null, none will be set.
	 * @param loginService
	 *            The login service used to login.
	 */
	public static Status showLoginModal(String message, Component parent, PasswordStore passwordStore,
			UserNameStore userNameStore, LoginService loginService) {
		JXLoginPane pane = makeLogin(message, passwordStore, userNameStore, loginService);
		return JXLoginPane.showLoginDialog(parent, pane);
	}

	/**
	 * Never roll your own security. This method returns a JFrame containing
	 * only a login, and when run it does not block. *
	 * 
	 * @param passwordStore
	 *            A password store uses the last successful login password to
	 *            pre-populate the password field. If null, none will be set.
	 * @param userNameStore
	 *            A username store uses the last successful login username to
	 *            pre-populate the username field. If null, none will be set.
	 * @param loginService
	 *            The login service used to login.
	 */
	public static JFrame showLoginModeless(String message, PasswordStore passwordStore, UserNameStore userNameStore,
			LoginService loginService) {
		JXLoginPane pane = makeLogin(message, passwordStore, userNameStore, loginService);
		return JXLoginPane.showLoginFrame(pane);
	}

}
