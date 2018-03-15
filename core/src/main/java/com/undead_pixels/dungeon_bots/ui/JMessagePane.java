package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.time.LocalDateTime;

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


	private JMessagePane(){
		
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


	/**Adds a plain text message.*/
	public void message(String text, Color color) {
		StyledDocument doc  = messagePane.getStyledDocument();
		Style style = doc.addStyle(null, null);
		StyleConstants.setForeground(style,  color);
		try {
			doc.insertString(doc.getLength(),  LocalDateTime.now().toLocalTime() + "\n" + text + "\n",  style);
		} catch (BadLocationException e) {			
			e.printStackTrace();
		}
		
	}


	/**Adds the sender's image, followed by a plain text message.*/
	public void message(HasImage sender, String text, Color color) {

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


}
