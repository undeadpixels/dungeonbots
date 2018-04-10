/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.JEntityEditor.State;

/**
 *
 */
public class JEntityPropertyControl {

	private boolean changed = false;
	private final Entity entity;
	private final SecurityLevel security;
	private final ArrayList<CheckboxController> checkboxes = new ArrayList<>();


	public JEntityPropertyControl(Entity entity, SecurityLevel level) {
		this.entity = entity;
		this.security = level;
	}


	public JComponent create() {

		// Valid Entity permissions:
		// REPL
		// SCRIPT_EDITOR
		// PROPERTIES
		// ENTITY_EDITOR
		// SELECTION

		Box propertiesPanel = new Box(BoxLayout.Y_AXIS);

		JLabel imgLabel = new JLabel(new ImageIcon(entity.getImage().getScaledInstance(256, 256, BufferedImage.SCALE_FAST)));
		
		Box pnlHeaderText = new Box(BoxLayout.Y_AXIS);
		pnlHeaderText.add(new JLabel(entity.getName()));
		pnlHeaderText.add(new JLabel("("+entity.getPosition().x+", "+entity.getPosition().y+")"));

		Box pnlHeader = new Box(BoxLayout.X_AXIS);
		pnlHeader.add(imgLabel);
		pnlHeader.add(pnlHeaderText);
		
		
		checkboxes.add(new CheckboxController("Selection", "SELECTION",
				"Access to whether the entity can be selected."));
		checkboxes.add(new CheckboxController("Entity editor", "ENTITY_EDITOR",
				"Access to the entity editing dialog (the dialog you're looking at right now)."));
		checkboxes.add(new CheckboxController("Command line", "REPL",
				"Access level for the command line in the entity editing dialog."));
		checkboxes.add(new CheckboxController("Script editor", "SCRIPT_EDITOR",
				"Access level for the script editor in the entity editing dialog."));
		checkboxes.add(new CheckboxController("Properties editor", "PROPERTIES",
				"Access to the property editor in the entity editing dialog (you're looking at the property editor right now)."));


		propertiesPanel.add(pnlHeader);
		
		for(CheckboxController c : checkboxes) {
			propertiesPanel.add(c.makeCheckbox());
		}


		return propertiesPanel;
	}
	
	private class CheckboxController {
		private String title, flagName, description;
		private JCheckBox checkBox;

		public CheckboxController(String title, String flagName, String description) {
			super();
			this.title = title;
			this.flagName = flagName;
			this.description = description;
		}
		
		public JCheckBox makeCheckbox() {
			checkBox = new JCheckBox(title, entity.getPermission(flagName).level < SecurityLevel.AUTHOR.level);
			checkBox.setToolTipText(description);
			return checkBox;
			
		}

		/**
		 * @param state 
		 */
		public void save (State state) {
			state.setPermission(flagName, checkBox.isSelected() ? SecurityLevel.NONE : SecurityLevel.AUTHOR);
		}
	}

	/**
	 * @param state 
	 * 
	 */
	public void save (State state) {
		for(CheckboxController c: checkboxes) {
			c.save(state);
		}
	}
}
