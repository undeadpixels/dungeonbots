/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Sign;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.JEntityEditor.State;

/**
 * @author kevin
 *
 */
public class JSignEditor extends Box {

	private State state;
	private JTextArea textEdit;

	/**
	 * @param state
	 * @param entity
	 * @param security
	 */
	public JSignEditor(State state, Sign entity, SecurityLevel security) {
		super(BoxLayout.Y_AXIS);
		
		textEdit = new JTextArea(entity.getMessage());
		
		String prompt;
		if(security == SecurityLevel.AUTHOR) {
			prompt = "Edit this sign:";
			textEdit.setEditable(true);
		} else {
			prompt = "This sign says:";
			textEdit.setEditable(false);
		}
		
		this.setBorder(BorderFactory.createTitledBorder(prompt));
		
		this.add(textEdit);
		
		this.state = state;
		
	}

	/**
	 * 
	 */
	public void save () {
		state.setSignText(textEdit.getText());
	}
	
}
