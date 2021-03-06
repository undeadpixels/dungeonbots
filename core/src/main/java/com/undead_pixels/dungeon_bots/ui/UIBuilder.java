package com.undead_pixels.dungeon_bots.ui;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.border.Border;


public class UIBuilder {

	protected static final String FIELD_ACTION = "action";
	protected static final String FIELD_ACTION_COMMAND = "action_command";
	protected static final String FIELD_ACTION_LISTENER = "action_command_listener";
	protected static final String FIELD_ALIGNMENT_X = "alignment_x";
	protected static final String FIELD_ALIGNMENT_Y = "alignment_x";
	protected static final String FIELD_BACKGROUND = "background";
	protected static final String FIELD_BORDER = "border";
	protected static final String FIELD_ENABLED = "enabled";
	protected static final String FIELD_FOCUSABLE = "focusable";
	protected static final String FIELD_FOREGROUND = "foreground";
	protected static final String FIELD_HORIZONTAL_TEXT_POSITION = "horizontal_text_position";
	protected static final String FIELD_HOTKEY = "hotkey";
	protected static final String FIELD_IMAGE = "image";
	protected static final String FIELD_INSETS = "insets";
	protected static final String FIELD_MAX_SIZE = "max_size";
	protected static final String FIELD_MIN_SIZE = "min_size";
	protected static final String FIELD_MNEMONIC = "mnemonic";
	protected static final String FIELD_PREFERRED_SIZE = "preferred_size";
	protected static final String FIELD_TEXT = "text";
	protected static final String FIELD_TOOLTIP = "tooltip";
	protected static final String FIELD_VERTICAL_TEXT_POSITION = "vertical_text_position";
	protected static final int DEFAULT_PREFERRED_WIDTH = -1;
	protected static final int DEFAULT_PREFERRED_HEIGHT = -1;

	/**
	 * Whether or not verbose messages will be printed. Verbose messages include:
	 * <p>
	 * Advice messages of missing image file resources.
	 */
	public static boolean verbose = false;


	/**
	 * A class whose sole purpose is to assure that buttons get filled by their
	 * images, and the images shrink with the buttons.
	 */
	private static class ResizingIcon extends ImageIcon implements ComponentListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private Image _OriginalImage;
		private AbstractButton _Host;


		public ResizingIcon(Image image, AbstractButton host) {
			this._OriginalImage = image;
			(this._Host = host).addComponentListener(this);
			resize();
		}


		private void resize() {
			int width = _Host.getWidth();
			int height = _Host.getHeight();
			Insets margin = _Host.getMargin();
			Image rescaledImage = _OriginalImage.getScaledInstance(width - (margin.left + margin.right + 8),
					height - (margin.top + margin.bottom + 8), Image.SCALE_SMOOTH);
			setImage(rescaledImage);
		}


		@Override
		public void componentHidden(ComponentEvent arg0) {
			// Do nothing.
		}


		@Override
		public void componentMoved(ComponentEvent arg0) {
			// Do nothing.
		}


		@Override
		public void componentResized(ComponentEvent arg0) {
			resize();
		}


		@Override
		public void componentShown(ComponentEvent arg0) {
			resize();
		}
	}


	public static final class LabelBuilder {

		protected static abstract class PropertyBuilder<P> {

			protected final P value;


			public PropertyBuilder(P value) {
				this.value = value;
			}


			public final void apply(JLabel label) {
				apply(label, value);
			}


			protected abstract void apply(JLabel label, P value);
		}


		protected HashMap<String, PropertyBuilder<?>> properties = new HashMap<String, PropertyBuilder<?>>();


		public final void reset() {
			properties.clear();
		}


		public final LabelBuilder alignmentX(float alignment) {
			properties.put(FIELD_ALIGNMENT_X, new PropertyBuilder<Float>(alignment) {

				@Override
				public void apply(JLabel label, Float value) {
					label.setAlignmentX(value);
				}
			});
			return this;
		}


		public final LabelBuilder alignmentY(float alignment) {
			properties.put(FIELD_ALIGNMENT_Y, new PropertyBuilder<Float>(alignment) {

				@Override
				public void apply(JLabel label, Float value) {
					label.setAlignmentY(value);
				}
			});
			return this;
		}


		/**Specifies the background color.*/
		public final LabelBuilder background(Color color) {
			properties.put(FIELD_BACKGROUND, new PropertyBuilder<Color>(color) {

				@Override
				protected void apply(JLabel label, Color color) {
					label.setBackground(color);
				}
			});
			return this;
		}


		public final LabelBuilder border(Border border) {
			properties.put(FIELD_BORDER, new PropertyBuilder<Border>(border) {

				@Override
				protected void apply(JLabel label, Border value) {
					label.setBorder(value);
				}
			});
			return this;
		}


		public final LabelBuilder enabled(boolean value) {
			properties.put(FIELD_ENABLED, new PropertyBuilder<Boolean>(value) {

				@Override
				public void apply(JLabel label, Boolean value) {
					label.setEnabled(value);
				}

			});
			return this;
		}


		/** Sets focusability as specified. */
		public final LabelBuilder focusable(boolean focusable) {
			properties.put(FIELD_FOCUSABLE, new PropertyBuilder<Boolean>(focusable) {

				@Override
				public void apply(JLabel label, Boolean value) {
					label.setFocusable(value);
				}

			});
			return this;
		}


		/**Specifies the foreground color.*/
		public final LabelBuilder foreground(Color color) {
			properties.put(FIELD_FOREGROUND, new PropertyBuilder<Color>(color) {

				@Override
				protected void apply(JLabel label, Color color) {
					label.setForeground(color);
				}
			});
			return this;
		}


		/**
		 * Specifies an image from the given filename to be displayed. If the
		 * file could not be opened, sets the displayed text to the filename.
		 * The image will fill the entire button.
		 */
		public final LabelBuilder image(String filename) {
			return image(UIBuilder.getImage(filename));
		}


		/**
		 * Specifies an image to be displayed. The image will fill the entire
		 * button.
		 */
		public final LabelBuilder image(Image image) {
			properties.put(FIELD_IMAGE, new PropertyBuilder<Image>(image) {

				@Override
				public void apply(JLabel label, Image img) {
					label.setIcon(new ImageIcon(img));
				}
			});
			return this;
		}


		/** Sets the maximum height as indicated. */
		public LabelBuilder maxHeight(int height) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingMax = (PropertyBuilder<Dimension>) properties.get(FIELD_MAX_SIZE);
			if (existingMax == null)
				return maxSize(new Dimension(-1, height));
			else
				return maxSize(new Dimension(existingMax.value.width, height));
		}


		/** Sets the maximum size as indicated. */
		public LabelBuilder maxSize(int width, int height) {
			return maxSize(new Dimension(width, height));
		}


		/** Sets the maximum size as indicated. */
		public LabelBuilder maxSize(Dimension size) {
			properties.put(FIELD_MAX_SIZE, new PropertyBuilder<Dimension>(size) {

				@Override
				public void apply(JLabel label, Dimension value) {
					label.setMaximumSize(value);
				}

			});
			return this;
		}


		/** Sets the maximum width as indicated. */
		public LabelBuilder maxWidth(int width) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingMax = (PropertyBuilder<Dimension>) properties.get(FIELD_MAX_SIZE);
			if (existingMax == null)
				return maxSize(new Dimension(width, -1));
			else
				return maxSize(new Dimension(width, existingMax.value.height));
		}


		/** Sets the minimum height as indicated. */
		public LabelBuilder minHeight(int height) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingmin = (PropertyBuilder<Dimension>) properties.get(FIELD_MIN_SIZE);
			if (existingmin == null)
				return minSize(new Dimension(-1, height));
			else
				return minSize(new Dimension(existingmin.value.width, height));
		}


		/** Sets the minimum size as indicated. */
		public LabelBuilder minSize(int width, int height) {
			return minSize(new Dimension(width, height));
		}


		/** Sets the minimum size as indicated. */
		public LabelBuilder minSize(Dimension size) {
			properties.put(FIELD_MIN_SIZE, new PropertyBuilder<Dimension>(size) {

				@Override
				public void apply(JLabel label, Dimension value) {
					label.setMinimumSize(value);
				}

			});
			return this;
		}


		/** Sets the minimum width as indicated. */
		public LabelBuilder minWidth(int width) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingmin = (PropertyBuilder<Dimension>) properties.get(FIELD_MIN_SIZE);
			if (existingmin == null)
				return minSize(new Dimension(width, -1));
			else
				return minSize(new Dimension(width, existingmin.value.height));
		}


		/** Sets the preferred height as indicated. */
		public LabelBuilder prefHeight(int height) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingpref = (PropertyBuilder<Dimension>) properties.get(FIELD_PREFERRED_SIZE);
			if (existingpref == null)
				return prefSize(new Dimension(-1, height));
			else
				return prefSize(new Dimension(existingpref.value.width, height));
		}


		/** Sets the preferred size as indicated. */
		public LabelBuilder preferredSize(int width, int height) {
			return prefSize(new Dimension(width, height));
		}


		/** Sets the preferred size as indicated. */
		public LabelBuilder prefSize(Dimension size) {
			properties.put(FIELD_PREFERRED_SIZE, new PropertyBuilder<Dimension>(size) {

				@Override
				public void apply(JLabel label, Dimension value) {
					label.setPreferredSize(value);
				}

			});
			return this;
		}


		/** Sets the preferred width as indicated. */
		public LabelBuilder prefWidth(int width) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingpref = (PropertyBuilder<Dimension>) properties.get(FIELD_PREFERRED_SIZE);
			if (existingpref == null)
				return prefSize(new Dimension(width, -1));
			else
				return prefSize(new Dimension(width, existingpref.value.height));
		}


		/**
		 * Specifies the given text to be displayed by buttons created by this
		 * builder.
		 */
		public final LabelBuilder text(String text) {
			properties.put(FIELD_TEXT, new PropertyBuilder<String>(text) {

				@Override
				public void apply(JLabel label, String txt) {
					label.setText(txt);
				}

			});
			return this;
		}


		/** Adds the given tooltip text to buttons created by this builder. */
		public final LabelBuilder toolTip(String toolTipText) {
			properties.put(FIELD_TOOLTIP, new PropertyBuilder<String>(toolTipText) {

				@Override
				public void apply(JLabel label, String t) {
					label.setToolTipText(t);
				}

			});
			return this;
		}


		public final JLabel create() {
			JLabel lbl = new JLabel();
			for (PropertyBuilder<?> p : properties.values())
				p.apply(lbl);
			return lbl;
		}


	}


	/**
	 * blah blah blah. Try this:
	 * <p>
	 * JButton button = UIBuilder.buildButton().text("I'm a button.").create();
	 * <p>
	 * That will create a button containing the text "I'm a button."
	 */
	public static abstract class ButtonBuilder<T extends AbstractButton> {

		protected static abstract class PropertyBuilder<P> {

			protected final P value;


			public PropertyBuilder(P value) {
				this.value = value;
			}


			public final void apply(AbstractButton bttn) {
				apply(bttn, value);
			}


			protected abstract void apply(AbstractButton bttn, P value);
		}


		// protected HashMap<String, Object> settings = new HashMap<String,
		// Object>();
		protected final HashMap<String, PropertyBuilder<?>> properties = new HashMap<String, PropertyBuilder<?>>();


		/** Clears all settings from this ButtonBuilder. */
		public final void reset() {
			properties.clear();
		}


		public final ButtonBuilder<T> action(Action action) {
			properties.put(FIELD_ACTION, new PropertyBuilder<Action>(action) {

				@Override
				public void apply(AbstractButton bttn, Action value) {
					bttn.setAction(value);
				}
			});
			return this;
		}


		/** Adds the given action listener. */
		public ButtonBuilder<T> action(ActionListener listener) {
			properties.put(FIELD_ACTION_LISTENER, new PropertyBuilder<ActionListener>(listener) {

				@Override
				public void apply(AbstractButton bttn, ActionListener value) {
					bttn.addActionListener(value);
				}

			});
			return this;
		}


		/**
		 * Specifies the action command, and adds the given listener to buttons
		 * created by this builder.
		 */
		public final ButtonBuilder<T> action(String command, ActionListener listener) {
			properties.put(FIELD_ACTION_COMMAND, new PropertyBuilder<String>(command) {

				@Override
				public void apply(AbstractButton bttn, String value) {
					bttn.setActionCommand(value);
				}

			});
			properties.put(FIELD_ACTION_LISTENER, new PropertyBuilder<ActionListener>(listener) {

				@Override
				public void apply(AbstractButton bttn, ActionListener value) {
					bttn.addActionListener(value);
				}

			});
			return this;
		}


		public final ButtonBuilder<T> alignmentX(float alignment) {
			properties.put(FIELD_ALIGNMENT_X, new PropertyBuilder<Float>(alignment) {

				@Override
				public void apply(AbstractButton bttn, Float value) {
					bttn.setAlignmentX(value);
				}
			});
			return this;
		}


		public final ButtonBuilder<T> alignmentY(float alignment) {
			properties.put(FIELD_ALIGNMENT_Y, new PropertyBuilder<Float>(alignment) {

				@Override
				public void apply(AbstractButton bttn, Float value) {
					bttn.setAlignmentY(value);
				}
			});
			return this;
		}


		/**Specifies the background color.*/
		public final ButtonBuilder<T> background(Color color) {
			properties.put(FIELD_BACKGROUND, new PropertyBuilder<Color>(color) {

				@Override
				protected void apply(AbstractButton bttn, Color color) {
					bttn.setBackground(color);
				}
			});
			return this;
		}


		public final ButtonBuilder<T> border(Border border) {
			properties.put(FIELD_BORDER, new PropertyBuilder<Border>(border) {

				@Override
				protected void apply(AbstractButton bttn, Border value) {
					bttn.setBorder(value);
				}
			});
			return this;
		}


		public final ButtonBuilder<T> enabled(boolean value) {
			properties.put(FIELD_ENABLED, new PropertyBuilder<Boolean>(value) {

				@Override
				public void apply(AbstractButton bttn, Boolean value) {
					bttn.setEnabled(value);
				}

			});
			return this;
		}


		/** Sets focusability as specified. */
		public final ButtonBuilder<T> focusable(boolean focusable) {
			properties.put(FIELD_FOCUSABLE, new PropertyBuilder<Boolean>(focusable) {

				@Override
				public void apply(AbstractButton bttn, Boolean value) {
					bttn.setFocusable(value);
				}

			});
			return this;
		}


		/**Specifies the foreground color.*/
		public final ButtonBuilder<T> foreground(Color color) {
			properties.put(FIELD_FOREGROUND, new PropertyBuilder<Color>(color) {

				@Override
				protected void apply(AbstractButton bttn, Color color) {
					bttn.setForeground(color);
				}
			});
			return this;
		}


		/**
		 * Specifies the hotkey for buttons created by this builder (note that
		 * this may result in multiple buttons having the same hotkey).
		 * 
		 * @param keyEvent
		 *            This KeyEvent int that specifies the key press, such as
		 *            KeyEvent.VK_A.
		 */
		public final ButtonBuilder<T> hotkey(int keyEvent, boolean shift, boolean ctrl, boolean alt, boolean meta,
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
			return hotkey(keyEvent, mod);
		}


		/**
		 * Specifies the hotkey for buttons created by this builder (note that
		 * this may result in multiple buttons having the same hotkey).
		 * 
		 * @param keyEvent
		 *            This KeyEvent int that specifies the key press, such as
		 *            KeyEvent.VK_A.
		 */
		public final ButtonBuilder<T> hotkey(int keyEvent) {
			return hotkey(keyEvent, 0);
		}


		/**
		 * Specifies the hotkey for buttons created by this builder (note that
		 * this may result in multiple buttons having the same hotkey).
		 * 
		 * @param keyEvent
		 *            This KeyEvent int that specifies the key press, such as
		 *            KeyEvent.VK_A.
		 */
		public final ButtonBuilder<T> hotkey(int keyEvent, int modifiers) {
			return hotkey(KeyStroke.getKeyStroke(keyEvent, modifiers));
		}


		/**Specifies the horizontal position of text in relation to set images.  
		 * The value supplied should be one of the SwingConstants.  Default value is 
		 * SwingConstants.LEFT.*/
		public final ButtonBuilder<T> horizontalTextPosition(int swingConstant) {
			properties.put(FIELD_HORIZONTAL_TEXT_POSITION, new PropertyBuilder<Integer>(swingConstant) {

				@Override
				protected void apply(AbstractButton bttn, Integer value) {
					bttn.setHorizontalTextPosition(swingConstant);
				}

			});
			return this;
		}


		/**
		 * Specifies the hotkey for buttons created by this builder (note that
		 * this may result in multiple buttons having the same hotkey).
		 */
		public final ButtonBuilder<T> hotkey(KeyStroke hotKey) {

			properties.put(FIELD_HOTKEY, new PropertyBuilder<KeyStroke>(hotKey) {

				@Override
				public void apply(AbstractButton bttn, KeyStroke hotkey) {
					bttn.registerKeyboardAction(
							bttn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
							KeyStroke.getKeyStroke(hotkey.getKeyCode(), hotkey.getModifiers(), false),
							JComponent.WHEN_IN_FOCUSED_WINDOW);
					bttn.registerKeyboardAction(
							bttn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
							KeyStroke.getKeyStroke(hotkey.getKeyCode(), hotkey.getModifiers(), true),
							JComponent.WHEN_IN_FOCUSED_WINDOW);
				}

			});
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
		 * Specifies an image from the given filename to be displayed. If the
		 * file could not be opened, sets the displayed text to the filename.
		 * The image will fill the entire button.
		 */
		public final ButtonBuilder<T> image(String filename) {
			return image(filename, true);
		}


		/**
		 * Specifies an image from the given filename will be displayed. If the
		 * file could not be opened, sets the displayed text to the filename.
		 * 
		 * @param proportional
		 *            If this value is true, the image will maintain
		 *            proportionality but be scaled as large as it can fit. If
		 *            false, the image will be stretched to completely fill.
		 */
		public final ButtonBuilder<T> image(String filename, boolean proportional) {
			Image img = UIBuilder.getImage(filename);
			if (img != null)
				return image(img, proportional);

			properties.put(FIELD_TEXT, new PropertyBuilder<String>(filename) {

				@Override
				public void apply(AbstractButton bttn, String altText) {
					bttn.setText(altText);
				}

			});
			return this;
		}


		/**
		 * Specifies an image to be displayed. The image will fill the entire
		 * button.
		 */
		public final ButtonBuilder<T> image(Image image) {
			return image(image, true);
		}


		/**
		 * Specifies an image to be displayed.
		 * 
		 * @param proportional
		 *            If this value is true, the image will maintain
		 *            proportionality but be scaled as large as it can fit. If
		 *            false, the image will be stretched to completely fill. *
		 */
		public final ButtonBuilder<T> image(Image image, boolean proportional) {
			properties.put(FIELD_IMAGE,
					new PropertyBuilder<ImageProportionality>(new ImageProportionality(image, proportional)) {

						@Override
						public void apply(AbstractButton bttn, ImageProportionality ip) {
							if (ip.proportional)
								bttn.setIcon(new ImageIcon(ip.image));
							else
								bttn.setIcon(new ResizingIcon(ip.image, bttn));
						}

					});
			return this;
		}


		/** Sets the margin as indicated. */
		public final ButtonBuilder<T> margin(int top, int left, int bottom, int right) {
			return margin(new Insets(top, left, bottom, right));
		}


		/** Sets the margin as indicated. */
		public final ButtonBuilder<T> margin(Insets insets) {
			properties.put(FIELD_INSETS, new PropertyBuilder<Insets>(insets) {

				@Override
				public void apply(AbstractButton bttn, Insets value) {
					bttn.setMargin(value);
				}

			});
			return this;
		}


		/** Sets the maximum height as indicated. */
		public ButtonBuilder<T> maxHeight(int height) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingMax = (PropertyBuilder<Dimension>) properties.get(FIELD_MAX_SIZE);
			if (existingMax == null)
				return maxSize(new Dimension(-1, height));
			else
				return maxSize(new Dimension(existingMax.value.width, height));
		}


		/** Sets the maximum size as indicated. */
		public ButtonBuilder<T> maxSize(int width, int height) {
			return maxSize(new Dimension(width, height));
		}


		/** Sets the maximum size as indicated. */
		public ButtonBuilder<T> maxSize(Dimension size) {
			properties.put(FIELD_MAX_SIZE, new PropertyBuilder<Dimension>(size) {

				@Override
				public void apply(AbstractButton bttn, Dimension value) {
					bttn.setMaximumSize(value);
				}

			});
			return this;
		}


		/** Sets the maximum width as indicated. */
		public ButtonBuilder<T> maxWidth(int width) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingMax = (PropertyBuilder<Dimension>) properties.get(FIELD_MAX_SIZE);
			if (existingMax == null)
				return maxSize(new Dimension(width, -1));
			else
				return maxSize(new Dimension(width, existingMax.value.height));
		}


		/** Sets the minimum height as indicated. */
		public ButtonBuilder<T> minHeight(int height) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingmin = (PropertyBuilder<Dimension>) properties.get(FIELD_MIN_SIZE);
			if (existingmin == null)
				return minSize(new Dimension(-1, height));
			else
				return minSize(new Dimension(existingmin.value.width, height));
		}


		/** Sets the minimum size as indicated. */
		public ButtonBuilder<T> minSize(int width, int height) {
			return minSize(new Dimension(width, height));
		}


		/** Sets the minimum size as indicated. */
		public ButtonBuilder<T> minSize(Dimension size) {
			properties.put(FIELD_MIN_SIZE, new PropertyBuilder<Dimension>(size) {

				@Override
				public void apply(AbstractButton bttn, Dimension value) {
					bttn.setMinimumSize(value);
				}

			});
			return this;
		}


		/** Sets the minimum width as indicated. */
		public ButtonBuilder<T> minWidth(int width) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingmin = (PropertyBuilder<Dimension>) properties.get(FIELD_MIN_SIZE);
			if (existingmin == null)
				return minSize(new Dimension(width, -1));
			else
				return minSize(new Dimension(width, existingmin.value.height));
		}


		public final ButtonBuilder<T> mnemonic(char mnemonic) {
			properties.put(FIELD_MNEMONIC, new PropertyBuilder<Character>(mnemonic) {

				@Override
				public void apply(AbstractButton bttn, Character value) {
					bttn.setMnemonic(value);
				}
			});
			return this;
		}


		/** Sets the preferred height as indicated. */
		public ButtonBuilder<T> prefHeight(int height) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingpref = (PropertyBuilder<Dimension>) properties.get(FIELD_PREFERRED_SIZE);
			if (existingpref == null)
				return prefSize(new Dimension(-1, height));
			else
				return prefSize(new Dimension(existingpref.value.width, height));
		}


		/** Sets the preferred size as indicated. */
		public ButtonBuilder<T> preferredSize(int width, int height) {
			return prefSize(new Dimension(width, height));
		}


		/** Sets the preferred size as indicated. */
		public ButtonBuilder<T> prefSize(Dimension size) {
			properties.put(FIELD_PREFERRED_SIZE, new PropertyBuilder<Dimension>(size) {

				@Override
				public void apply(AbstractButton bttn, Dimension value) {
					bttn.setPreferredSize(value);
				}

			});
			return this;
		}


		/** Sets the preferred width as indicated. */
		public ButtonBuilder<T> prefWidth(int width) {
			@SuppressWarnings("unchecked")
			PropertyBuilder<Dimension> existingpref = (PropertyBuilder<Dimension>) properties.get(FIELD_PREFERRED_SIZE);
			if (existingpref == null)
				return prefSize(new Dimension(width, -1));
			else
				return prefSize(new Dimension(width, existingpref.value.height));
		}


		/**
		 * Specifies the given text to be displayed by buttons created by this
		 * builder.
		 */
		public final ButtonBuilder<T> text(String text) {
			properties.put(FIELD_TEXT, new PropertyBuilder<String>(text) {

				@Override
				public void apply(AbstractButton bttn, String txt) {
					bttn.setText(txt);
				}

			});
			return this;
		}


		/**Specifies the horizontal and vertical position of text in relation to set images.  
		 * The values supplied should be one of the SwingConstants.  Default values are 
		 * SwingConstants.LEFT and SwingConstants.CENTER.*/
		public final ButtonBuilder<T> textPosition(int horizontal, int vertical) {
			properties.put(FIELD_HORIZONTAL_TEXT_POSITION, new PropertyBuilder<Integer>(horizontal) {

				@Override
				protected void apply(AbstractButton bttn, Integer horizontal) {
					bttn.setHorizontalTextPosition(horizontal);
				}

			});
			properties.put(FIELD_VERTICAL_TEXT_POSITION, new PropertyBuilder<Integer>(vertical) {

				@Override
				protected void apply(AbstractButton bttn, Integer vertical) {
					bttn.setVerticalTextPosition(vertical);
				}

			});
			return this;
		}


		/** Adds the given tooltip text to buttons created by this builder. */
		public final ButtonBuilder<T> toolTip(String toolTipText) {
			properties.put(FIELD_TOOLTIP, new PropertyBuilder<String>(toolTipText) {

				@Override
				public void apply(AbstractButton bttn, String t) {
					bttn.setToolTipText(t);
				}

			});
			return this;
		}


		/**Specifies the vertical position of text in relation to set images.  
		 * The value supplied should be one of the SwingConstants.  Default value is 
		 * SwingConstants.CENTER.*/
		public final ButtonBuilder<T> verticalTextPosition(int swingConstant) {
			properties.put(FIELD_VERTICAL_TEXT_POSITION, new PropertyBuilder<Integer>(swingConstant) {

				@Override
				protected void apply(AbstractButton bttn, Integer value) {
					bttn.setVerticalTextPosition(swingConstant);
				}

			});
			return this;
		}


		/**
		 * Create an uninitialized button as it is defined in Swing, for example
		 * a JButton or a JToggleButton. Settings will be applied after the
		 * button is instantiated.
		 */
		protected abstract T createUninitialized();


		public final T create() {

			// Create the base object.
			T bttn = createUninitialized();


			// Apply all set properties to the new button.
			for (PropertyBuilder<?> p : properties.values())
				p.apply(bttn);

			// The 'enter' key should do the same as the 'space' key.
			// TODO: Should this be hard-coded, or an option?
			bttn.registerKeyboardAction(bttn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);
			bttn.registerKeyboardAction(bttn.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);

			return bttn;
		}

	}


	public static class ToggleButtonBuilder extends ButtonBuilder<JToggleButton> {

		@Override
		protected JToggleButton createUninitialized() {
			return new JToggleButton();
		}
	}


	public final static class MenuBuilder extends ButtonBuilder<JMenu> {

		protected static final String FIELD_ACCELERATOR = "accelerator";


		// TODO: a templating system for child menus?

		@Override
		protected JMenu createUninitialized() {
			return new JMenu();
		}


		public final MenuBuilder accelerator(KeyStroke accelerator) {
			properties.put(FIELD_ACCELERATOR, new PropertyBuilder<KeyStroke>(accelerator) {

				@Override
				protected void apply(AbstractButton bttn, KeyStroke value) {
					((JMenu) bttn).setAccelerator(value);
				}

			});
			return this;
		}


		public final MenuBuilder accelerator(int key, int mask) {
			return accelerator(KeyStroke.getKeyStroke(key, mask));
		}
	}


	public final static class MenuItemBuilder extends ButtonBuilder<JMenuItem> {


		protected static final String FIELD_ACCELERATOR = "accelerator";


		@Override
		protected JMenuItem createUninitialized() {
			return new JMenuItem();
		}


		public final MenuItemBuilder accelerator(KeyStroke accelerator) {
			properties.put(FIELD_ACCELERATOR, new PropertyBuilder<KeyStroke>(accelerator) {

				@Override
				protected void apply(AbstractButton bttn, KeyStroke value) {
					((JMenuItem) bttn).setAccelerator(value);
				}

			});
			return this;
		}


		public final MenuItemBuilder accelerator(int key, int mask) {
			return accelerator(KeyStroke.getKeyStroke(key, mask));
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
			protected JButton createUninitialized() {
				return new JButton();
			}
		};
	}


	/**Returns a builder for a JLabel.*/
	public static LabelBuilder buildLabel() {
		return new LabelBuilder();
	}


	/**Returns a builder for a JMenu.*/
	public static MenuBuilder buildMenu() {
		return new MenuBuilder();
	}


	/**
	 * Returns a builder for a JMenuItem.
	 */
	public static MenuItemBuilder buildMenuItem() {
		return new MenuItemBuilder();
	}


	/**
	 * Returns a builder for a JToggleButton. The builder pattern will allow the
	 * following call:
	 * <p>
	 * JToggleButton t = buildToggleButton().text("I'm a
	 * button").action("CLICK", listener).create();
	 */
	public static ToggleButtonBuilder buildToggleButton() {
		return new ToggleButtonBuilder();
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
		JToggleButton toggler = UIBuilder.buildToggleButton().action("TOGGLE", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!e.getActionCommand().equals("TOGGLE"))
					return;
				collapser.setCollapsed(!collapser.isCollapsed());
				lbl.setText(collapser.isCollapsed() ? closedText : openText);
				((JToggleButton) e.getSource()).setText(collapser.isCollapsed() ? ">" : "v");
			}
		}).create();

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
		/* result.addComponentListener(new ComponentAdapter() {
		 * 
		 * @Override public void componentResized(ComponentEvent e) { if
		 * (e.getID() != ComponentEvent.COMPONENT_RESIZED) return; //int height
		 * = result.getHeight() - header.getHeight();
		 * collapser.setPreferredSize(new Dimension(result.getWidth(), 400));
		 * //System.out.println(e.getID() + " " + result.getSize().toString());
		 * } }); */

		return result;

	}


	/** The cached GUI images. */
	private static final HashMap<String, Image> _CachedImages = new HashMap<String, Image>();


	/**
	 * Gets an Image based on the image at the given location. Also, caches
	 * loaded images so that a call to the same image resource will not load it
	 * twice. If no image exists at the given location, returns null and prints
	 * a missing resource message to System.err (when verbose is on).
	 */
	public static Image getImage(String filename) {
		return getImage(filename, false);
	}


	/**
	 * Gets an Image based on the image at the given location. Also, caches
	 * loaded images so that a call to the same image resource will not load it
	 * twice. If no image exists at the given location, returns null and prints
	 * a missing resource message to System.err (when verbose is on).
	 * @param absolute Whether or not the absolute filename is given.  If true, 
	 * will simply look for the given filename.  If false, will look for the 
	 * file in the current working directory.
	 */
	public static Image getImage(String filename, boolean absolute) {
		filename = filename.toLowerCase();
		if (filename == null || filename.equals(""))
			return null;
		if (_CachedImages.containsKey(filename))
			return _CachedImages.get(filename);
		String path = absolute ? filename : System.getProperty("user.dir") + "/" + filename;
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(path));
		} catch (IOException ioex) {
			if (verbose)
				System.err.println("Image resource missing: " + filename + "\t" + ioex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (img != null)
			_CachedImages.put(filename, img);
		return img;
	}


	public static Image getImage(URL url) {
		if (url == null)
			return null;
		if (_CachedImages.containsKey(url.toString()))
			return _CachedImages.get(url.toString());
		BufferedImage img = null;
		try {
			img = ImageIO.read(url);
			if (img != null)
				_CachedImages.put(url.toString(), img);
			return img;
		} catch (Exception ex) {
			System.err.println("Image resource failed to download: " + url.toString() + "\t" + ex.getMessage());
		}
		return null;
	}


	private static final HashMap<String, Font> _Fonts = new HashMap<String, Font>();


	public static Font getFont(String filename) {
		return getFont(filename, false);
	}


	public static Font getFont(String filename, boolean absolute) {
		filename = filename.toLowerCase();
		if (filename == null || filename.equals(""))
			return null;
		if (_Fonts.containsKey(filename))
			return _Fonts.get(filename);
		String path = absolute ? filename : System.getProperty("user.dir") + "/" + filename;
		Font f = null;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(path));
			_Fonts.put(filename, f);
			return f;
		} catch (IOException ex) {
			if (verbose)
				System.err.println("Font resource missing: " + filename);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
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
	public static JXLoginPane.Status showLoginModal(String message, Component parent, PasswordStore passwordStore,
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


	


	// =======================================================
	// ========= UI Audio stuff ==============================
	// =======================================================

	public static Mix mix = new Mix();


	public static class Mix {

		public static float DEFAULT_GAIN = 0.0f;
		private float soundGain = DEFAULT_GAIN;
		private float musicGain = DEFAULT_GAIN;


		/**Returns the current music gain.*/
		public float getSoundGain() {
			return soundGain;
		}


		/**Sets the current music gain.*/
		public void setSoundGain(float gain) {
			soundGain = gain;
			for (Channel c : _PlayingSounds.values()) {
				soundGain = applyTo(c.clip, gain);
			}
		}


		/**Gets the current music gain.*/
		public float getMusicGain() {
			return musicGain;
		}


		/**Sets the current music gain.*/
		public void setMusicGain(float gain) {
			musicGain = gain;
			Channel c = _CurrentMusic;
			if (c != null)
				musicGain = applyTo(c.clip, gain);
		}


		/**Applies the gain setting to the indicated clip, returning the new gain level.*/
		static float applyTo(Clip clip, float gain) {
			FloatControl gainCtrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float v = Math.min(gainCtrl.getMaximum(), Math.max(gainCtrl.getMinimum(), gain));
			gainCtrl.setValue(v);
			return v;
		}
	}


	private static final HashMap<String, byte[]> _CachedSounds = new HashMap<String, byte[]>();
	private static Channel _CurrentMusic = null;
	private static final HashMap<Clip, Channel> _PlayingSounds = new HashMap<Clip, Channel>();


	/**Music files, being large, are not cached, and only one can play at a time.*/
	public static Clip playMusic(String filename) {
		if (_CurrentMusic != null) {
			_CurrentMusic.clip.stop();
			_CurrentMusic.clip.close();
			try {
				_CurrentMusic.stream.close();
			} catch (IOException e) {
				// Do nothing.
			}
		}

		String path = System.getProperty("user.dir" + "/" + filename);
		File file = new File(path);
		if (!file.exists())
			return null;
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(file);
			DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat());
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.addLineListener(soundController);
			clip.open(stream);
			Mix.applyTo(clip, mix.getMusicGain());
			clip.start();
			return clip;
		} catch (Exception e) {
			if (verbose)
				System.err.println("Could not play music: " + filename + " - " + e.getMessage());
			return null;
		}


	}


	/**Plays the sound in the given file.  If this is the first time the sound is played, caches it 
	 * for later re-use.*/
	public static Clip playSound(String filename) {
		byte[] bytes = getSound(filename);
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes));
			DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat());
			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.addLineListener(soundController);
			clip.open(stream);
			Mix.applyTo(clip, mix.getSoundGain());
			clip.start();
			_PlayingSounds.put(clip, new Channel(clip, stream));
			return clip;
		} catch (Exception e) {
			System.err.println("Could not play sound: " + filename + " - " + e.getMessage());
			return null;
		}
	}


	/**The job of this listener is simply to release resources when a sound is stopped.*/
	private static final LineListener soundController = new LineListener() {

		@Override
		public void update(LineEvent e) {
			LineEvent.Type type = (LineEvent.Type) e.getType();
			if (type == LineEvent.Type.STOP) {
				Clip clip = (Clip) e.getLine();
				clip.close();

				if (_CurrentMusic != null && _CurrentMusic.clip.equals(clip)) {
					try {
						_CurrentMusic.stream.close();
					} catch (IOException e1) {
						// Do nothing
					}
					_CurrentMusic = null;
				} else {
					Channel channel = _PlayingSounds.get(clip);
					try {
						channel.stream.close();
					} catch (IOException ex) {
						// Do nothing.
					}
					_PlayingSounds.remove(clip);
				}

			}

		}
	};


	/**A simple class associating a Clip with its audio stream, so both can be closed at the 
	 * same time when the clip is stopped.*/
	private static class Channel {

		public final Clip clip;
		public final AudioInputStream stream;


		public Channel(Clip clip, AudioInputStream stream) {
			this.clip = clip;
			this.stream = stream;
		}
	}


	/**Returns a sound in the form of raw bytes.*/
	public static byte[] getSound(String filename) {

		// If the memory is stored, just load it.
		byte[] bytes = _CachedSounds.get(filename);
		if (bytes != null)
			return bytes;

		// No such sound in memory. Load it from file.
		String path = System.getProperty("user.dir") + "/" + filename;
		File file = new File(path);
		if (!file.exists())
			return null;
		try (FileInputStream fis = new FileInputStream(file)) {
			bytes = new byte[(int) file.length()];
			fis.read(bytes);
		} catch (IOException e) {
			System.err.println("Could not read sound file: " + path);
			return null;
		}
		_CachedSounds.put(filename, bytes);
		return bytes;
	}


}
