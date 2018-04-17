package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.HasImage;

/**A simple control whose purpose is to receive and display messages.*/
public final class JMessagePane extends JPanel {
	
	private static class MessageInfo {
		Image img;
		String text;
		Color color;
		LoggingLevel type;
		public MessageInfo(Image img, String senderName, String text, Color color, LoggingLevel type) {
			super();
			String senderString = "";
			if(senderName != null) {
				senderString = " | "+senderName;
			}
			this.img = img.getScaledInstance(40, 40, Image.SCALE_FAST);;
			this.text = LocalDateTime.now().toLocalTime() + senderString + "\n" + (text == null ? "" : text);
			this.color = color;
			this.type = type;
		}
		
		@Deprecated
		public MessageInfo(Image img, int width, int height, LoggingLevel type) {
			super();
			this.img = img.getScaledInstance(width, height, Image.SCALE_FAST);;
			this.text = LocalDateTime.now().toLocalTime() + "\n";
			this.color = Color.lightGray;
			this.type = type;
		}
	}

	private static class LogLevelSelection {
		public final EnumSet<LoggingLevel> level;
		public final String description;
		
		public LogLevelSelection(EnumSet<LoggingLevel> level, String description) {
			super();
			this.level = level;
			this.description = description;
		}
		
		public String toString() {
			return description;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextPane messagePane;


	private JMessagePane() {
		setLayout(new BorderLayout());
		levelChooser = new JComboBox<>(new LogLevelSelection[] {
				new LogLevelSelection(EnumSet.allOf(LoggingLevel.class), "All (debug)"),
				new LogLevelSelection(EnumSet.of(LoggingLevel.ERROR, LoggingLevel.QUEST, LoggingLevel.GENERAL, LoggingLevel.STDOUT), "Normal"),
				new LogLevelSelection(EnumSet.of(LoggingLevel.ERROR, LoggingLevel.QUEST, LoggingLevel.STDOUT), "Reduced"),
				new LogLevelSelection(EnumSet.of(LoggingLevel.ERROR, LoggingLevel.QUEST), "Error and Quest"),
				new LogLevelSelection(EnumSet.of(LoggingLevel.ERROR), "Error Only"),
				new LogLevelSelection(EnumSet.of(LoggingLevel.QUEST), "Quest Only"),
		});
		levelChooser.setSelectedIndex(1);
		setAllowedTypes(((LogLevelSelection)levelChooser.getSelectedItem()).level);
		levelChooser.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged (ItemEvent e) {
				setAllowedTypes(((LogLevelSelection)levelChooser.getSelectedItem()).level);
			}
		});
		messagePane = new JTextPane();
		messagePane.setFocusable(false);
		messagePane.setText("");
		messagePane.setPreferredSize(new Dimension(250, -1));
		
		Box levelChooserBox = new Box(BoxLayout.X_AXIS);
		levelChooserBox.add(Box.createGlue());
		levelChooserBox.add(new JLabel("Log Level:"));
		levelChooserBox.add(levelChooser);
		levelChooserBox.add(Box.createGlue());
		levelChooser.setMaximumSize(new Dimension(-1, levelChooser.getPreferredSize().height));
		levelChooserBox.setMaximumSize(new Dimension(-1, levelChooser.getPreferredSize().height));
		levelChooserBox.setPreferredSize(new Dimension(-1, levelChooser.getPreferredSize().height));
		
		add(levelChooserBox, BorderLayout.NORTH);
		add(new JScrollPane(messagePane), BorderLayout.CENTER);
	}

	private JComboBox<LogLevelSelection> levelChooser = new JComboBox<>();
	private ArrayList<MessageInfo> allMessages = new ArrayList<>();
	private EnumSet<LoggingLevel> messageLevel = EnumSet.allOf(LoggingLevel.class);


	/**Returns the bitmask specifying what message types the message pane will display.  
	 * Default is verbose (meaning, displays everything).*/
	public synchronized EnumSet<LoggingLevel> getAllowedTypes() {
		return messageLevel;
	}


	/**Sets a bitmask specifying what message types the message pane will display.  
	 * Default is verbose (meaning, displays everything).*/
	public synchronized void setAllowedTypes(EnumSet<LoggingLevel> messageLevel) {
		this.messageLevel = messageLevel;
		
		if(messagePane != null) {
			this.messagePane.setText("");
			this.messagePane.removeAll();
			for(MessageInfo inf : allMessages) {
				insert(inf);
			}
		}
	}


	public static JMessagePane create() {
		JMessagePane result = new JMessagePane();
		return result;
	}
	
	private void insert(MessageInfo inf) {
		if(!messageLevel.contains(inf.type)) {
			return;
		}
		
		StyledDocument doc = messagePane.getStyledDocument();
		Style style = doc.addStyle(null, null);

		if(inf.img != null) {
			// First, insert the sender's image.
			JLabel lbl = new JLabel(new ImageIcon(inf.img));
			messagePane.select(doc.getLength(), doc.getLength());
			messagePane.insertComponent(lbl);
		}

		// Second, insert the text with the appropriate color.
		try {
			StyleConstants.setForeground(style, inf.color);
			doc.insertString(doc.getLength(), inf.text + "\n\n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		messagePane.select(doc.getLength(), doc.getLength());
	}


	/**Adds a plain text message with the given color and type.*/
	public synchronized void message(String text, Color color, LoggingLevel level) {
		message(null, text, color, level);
	}


	/**Adds a plain text message with the given color.  Presumes the MessageType is 'general'.*/
	public void message(String text, Color color) {
		message(text, color, LoggingLevel.GENERAL);
	}


	/**Adds the sender's image, followed by a plain text message.  Presumes the MessageType is 
	 * 'general.'*/
	public void message(HasImage sender, String text, Color color) {
		message(sender, text, color, LoggingLevel.GENERAL);
	}


	/**Adds the sender's image, followed by a plain text message.*/
	public synchronized void message(HasImage sender, String text, Color color, LoggingLevel type) {
		String senderName = null;
		if(sender instanceof World) {
			senderName = "World";
		} else if(sender instanceof Entity) {
			senderName = ((Entity)sender).getName();
		}
		MessageInfo msg = new MessageInfo(sender == null ? null : sender.getImage(),
				senderName, text, color, type);
		allMessages.add(msg);
		insert(msg);
	}

	/**Insert an image, scaled to the given width and height.*/
	public void message(Image image, int width, int height) {
		MessageInfo msg = new MessageInfo(image, width, height, LoggingLevel.GENERAL);
		allMessages.add(msg);
		insert(msg);
	}


	/**
	 * 
	 */
	public void reset () {
		allMessages.clear();
		setAllowedTypes(this.getAllowedTypes()); // reset the view as well
	}


}
