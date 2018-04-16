/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.scene.World.EntityEventType;
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
	
	private JComponent lazyCreated;


	public JEntityPropertyControl(Entity entity, SecurityLevel level) {
		this.entity = entity;
		this.security = level;
		
		entity.getWorld().listenTo(EntityEventType.ENTITY_MOVED, this, (e) -> updateBorderName());
	}
	
	private void updateBorderName() {
		if(lazyCreated != null) {
			lazyCreated.setBorder(BorderFactory.createTitledBorder(entity.getName()+" @ "+"("+entity.getPosition().x+", "+entity.getPosition().y+")"));
		}
	}


	public JComponent create() {
		if(lazyCreated != null) {
			return lazyCreated;
		}
		Box propertiesPanel = new Box(BoxLayout.Y_AXIS);
		propertiesPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		
		JLabel imgLabel = new JLabel(new ImageIcon(entity.getImage().getScaledInstance(128, 128, BufferedImage.SCALE_FAST)));
		imgLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		imgLabel.setHorizontalAlignment(JLabel.LEFT);
		
		propertiesPanel.add(imgLabel);
		
		JSeparator separator = new JSeparator();
		separator.setMaximumSize(new Dimension(99999, 15));
		propertiesPanel.add(separator);
		
		
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

		
		for(CheckboxController c : checkboxes) {
			propertiesPanel.add(c.makeCheckbox());
		}
		
		propertiesPanel.add(new JPanel()); // spacing
		
		JScrollPane scroller = new JScrollPane(propertiesPanel);

		lazyCreated = scroller;
		updateBorderName();

		return scroller;
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
		
		public JComponent makeCheckbox() {
			checkBox = new JCheckBox(title, entity.getPermission(flagName).level < SecurityLevel.AUTHOR.level);
			checkBox.setToolTipText(description);
			checkBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			//Box ret = new Box(BoxLayout.X_AXIS);
			//ret.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			//ret.add(checkBox);
			//ret.add(new JPanel());
			
			//ret.setBorder(BorderFactory.createEmptyBorder());
			
			//ret.setPreferredSize(checkBox.getPreferredSize());
			
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
