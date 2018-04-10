/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

/**
 *
 */
public class JEntityPropertyControl {

	private boolean changed = false;
	private final Entity entity;
	private final SecurityLevel security;


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

		JPanel propertiesPanel = new JPanel();

		propertiesPanel.setLayout(new VerticalLayout());


		JButton bttnImage = UIBuilder.buildButton().image(entity.getImage(), true).prefSize(new Dimension(300, 200))
				.create();
		JPanel pnlHeaderText = new JPanel(new VerticalLayout());
		pnlHeaderText.add(new JLabel(entity.getName()));
		pnlHeaderText.add(new JLabel(entity.getPosition().toString()));

		JPanel pnlHeader = new JPanel(new HorizontalLayout());
		pnlHeader.add(bttnImage);
		pnlHeader.add(pnlHeaderText);

		JPermissionTree permTree = new JPermissionTree();
		permTree.addPermission("Selection", entity.getPermission("SELECTION"),
				"Access to whether the entity can be selected.");
		permTree.addPermission("Entity editor", entity.getPermission("ENTITY_EDITOR"),
				"Access to the entity editing dialog (the dialog you're looking at right now).");
		permTree.addPermission("Command line", entity.getPermission("REPL"),
				"Access level for the command line in the entity editing dialog.");
		permTree.addPermission("Script editor", entity.getPermission("SCRIPT_EDITOR"),
				"Access level for the script editor in the entity editing dialog.");
		permTree.addPermission("Properties editor", entity.getPermission("PROPERTIES"),
				"Access to the property editor in the entity editing dialog (you're looking at the property editor right now).");


		propertiesPanel.add(pnlHeader);
		propertiesPanel.add(permTree);


		return propertiesPanel;
	}
}
