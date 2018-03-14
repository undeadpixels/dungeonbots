/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

/**
 *
 */
public class JEntityPropertyTab {
	
	public JEntityPropertyTab(Entity e, SecurityLevel level) {
		
	}
	
	public JComponent create() {
		JPanel propertiesPanel = new JPanel();

		propertiesPanel.setLayout(new BorderLayout());
		propertiesPanel.add(new JLabel("To be determined..."), BorderLayout.CENTER);
		
		return propertiesPanel;
	}
}
