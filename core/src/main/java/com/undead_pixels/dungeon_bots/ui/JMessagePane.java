package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.undead_pixels.dungeon_bots.scene.entities.HasImage;

/**A simple control whose purpose is to receive and display messages.*/
public final class JMessagePane extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextPane messagePane;


	public enum MessageType {
		Verbose(0b11111111111111111111111111111111), General(1), Debug(2), Error(4);

		private int _value;


		MessageType(int val) {
			_value = val;
		}


		public int getValue() {
			return _value;
		}


		public static List<MessageType> parseMessageTypes(int val) {
			ArrayList<MessageType> result = new ArrayList<MessageType>();
			for (MessageType mt : values())
				if ((val & mt.getValue()) != 0)
					result.add(mt);
			return result;
		}
	}


	private JMessagePane() {

	}


	private MessageType _MessageType = MessageType.Verbose;


	/**Returns the bitmask specifying what message types the message pane will display.  
	 * Default is verbose (meaning, displays everything).*/
	public MessageType getAllowedTypes() {
		return _MessageType;
	}


	/**Sets a bitmask specifying what message types the message pane will display.  
	 * Default is verbose (meaning, displays everything).*/
	public void setAllowedTypes(MessageType messageType) {
		this._MessageType = messageType;
	}


	public static JMessagePane create() {
		JMessagePane result = new JMessagePane();
		result.setLayout(new BorderLayout());
		result.messagePane = new JTextPane();
		result.messagePane.setFocusable(false);
		result.messagePane.setText("");
		result.messagePane.setPreferredSize(new Dimension(250, -1));
		result.add(new JScrollPane(result.messagePane));
		return result;
	}


	/**Adds a plain text message with the given color and type.*/
	public void message(String text, Color color, MessageType type) {
		StyledDocument doc = messagePane.getStyledDocument();
		Style style = doc.addStyle(null, null);
		StyleConstants.setForeground(style, color);
		try {
			doc.insertString(doc.getLength(), LocalDateTime.now().toLocalTime() + "\n" + text + "\n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}


	/**Adds a plain text message with the given color.  Presumes the MessageType is 'general'.*/
	public void message(String text, Color color) {
		message(text, color, MessageType.General);
	}


	/**Adds the sender's image, followed by a plain text message.  Presumes the MessageType is 
	 * 'general.'*/
	public void message(HasImage sender, String text, Color color) {
		message(sender, text, color, MessageType.General);
	}


	/**Adds the sender's image, followed by a plain text message.*/
	public void message(HasImage sender, String text, Color color, MessageType type) {

		// First, insert the sender's image.
		StyledDocument doc = messagePane.getStyledDocument();
		Style style = doc.addStyle(null, null);
		Image img = sender.getImage().getScaledInstance(40, 40, Image.SCALE_FAST);
		JLabel lbl = new JLabel(new ImageIcon(img));
		messagePane.insertComponent(lbl);

		// Second, insert the text with the appropriate color.
		StyleConstants.setForeground(style, color);
		try {
			doc.insertString(doc.getLength(), LocalDateTime.now().toLocalTime() + "\n" + text + "\n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**Insert an image, scaled to the given width and height.*/
	public void message(Image image, int width, int height) {
		message(new Image[] { image }, width, height);
	}


	/**Insert a set of images, scaled to the given width and height.*/
	public void message(Image[] images, int width, int height) {
		StyledDocument doc = messagePane.getStyledDocument();
		Style style = doc.addStyle(null, null);
		for (int i = 0; i < images.length; i++) {
			Image img = images[i].getScaledInstance(width, height, Image.SCALE_FAST);
			JLabel lbl = new JLabel(new ImageIcon(img));
			messagePane.insertComponent(lbl);
		}
		try {
			doc.insertString(doc.getLength(), LocalDateTime.now().toLocalTime() + "\n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}


}
