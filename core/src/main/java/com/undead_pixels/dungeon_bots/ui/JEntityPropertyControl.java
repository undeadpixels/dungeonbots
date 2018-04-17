/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

/**
 *
 */
public class JEntityPropertyControl {

	private boolean changed = false;
	private final JEntityEditor.State state;
	private JPermissionTree permissions = null;
	private JTextArea infoPanel = null;


	public JEntityPropertyControl(JEntityEditor.State state) {
		this.state = state;
	}


	public JComponent create() {

		JPanel propertiesPanel = new JPanel();

		propertiesPanel.setLayout(new BorderLayout());

		/* ActionListener controller = new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) { switch
		 * (e.getActionCommand()) { case "CHANGE_IMAGE": File f =
		 * FileControl.openImageDialog(propertiesPanel); if (f == null ||
		 * !f.exists()) return; Image img =
		 * UIBuilder.getImage(f.getAbsolutePath()); // TODO: implement image
		 * changing? default:
		 * 
		 * System.err.println("HAVE NOT IMPLEMENTED."); }
		 * 
		 * }
		 * 
		 * }; */

		JLabel lblImage = UIBuilder.buildLabel().image(state.image.getScaledInstance(150, 150, Image.SCALE_SMOOTH))
				.border(BorderFactory.createTitledBorder("Image")).create();
		lblImage.setOpaque(false);
		lblImage.setHorizontalAlignment(SwingConstants.CENTER);
		lblImage.setVerticalAlignment(SwingConstants.CENTER);
		// JButton bttnImage = UIBuilder.buildButton().image(entity.getImage(),
		// true).prefSize(new Dimension(300, 200)).action("CHANGE_IMAGE",
		// controller).create();
		JPanel pnlHeaderText = new JPanel(new VerticalLayout());
		JTextField fldName = new JTextField(20);
		fldName.setText(state.name);
		fldName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				state.name = fldName.getText();
			}


			@Override
			public void insertUpdate(DocumentEvent arg0) {
				state.name = fldName.getText();
			}


			@Override
			public void removeUpdate(DocumentEvent arg0) {
				state.name = fldName.getText();
			}
		});
		pnlHeaderText.add(fldName);
		pnlHeaderText.add(new JLabel("Position: " + state.position.x + ", " + state.position.y));
		pnlHeaderText.setBorder(BorderFactory.createTitledBorder("Info"));


		JPanel pnlHeader = new JPanel(new BorderLayout());
		// pnlHeader.add(bttnImage);
		pnlHeader.add(lblImage, BorderLayout.CENTER);
		pnlHeader.add(pnlHeaderText, BorderLayout.LINE_END);

		System.out.println("PERMISSIONS:");
		for (String p : state.permissions.keySet()){
			System.out.println(p);
		}
		permissions = new JPermissionTree();
		permissions.addPermission(Entity.PERMISSION_SELECTION, state.permissions.get("Selection"),
				"Access level for whether the entity can be selected.  Note that if a user cannot select the entity, the entity editor cannot be opened either.");
		permissions.addPermission(Entity.PERMISSION_ENTITY_EDITOR,
				state.permissions.get(Entity.PERMISSION_ENTITY_EDITOR),
				"Access level for the entity editing dialog (the dialog you're looking at right now).");
		permissions.addPermission(Entity.PERMISSION_COMMAND_LINE, state.permissions.get(Entity.PERMISSION_COMMAND_LINE),
				"Access level for the command line in the entity editing dialog.");
		permissions.addPermission(Entity.PERMISSION_SCRIPT_EDITOR,
				state.permissions.get(Entity.PERMISSION_SCRIPT_EDITOR),
				"Access level for the script editor in the entity editing dialog.");
		permissions.addPermission(Entity.PERMISSION_ADD_REMOVE_SCRIPTS,
				state.permissions.get(Entity.PERMISSION_ADD_REMOVE_SCRIPTS),
				"Access level for the property editor in the entity editing dialog (you're looking at the property editor right now).");
		permissions.addPermission(Entity.PERMISSION_PROPERTIES_EDITOR,
				state.permissions.get(Entity.PERMISSION_PROPERTIES_EDITOR),
				"Access level for the property editor in the entity editing dialog (you're looking at the property editor right now).");
		permissions.addPermission(Entity.PERMISSION_EDIT_HELP, state.permissions.get(Entity.PERMISSION_EDIT_HELP),
				"Access level for editing the help info for an entity.");
		permissions.setSecurityLevels(new SecurityLevel[] { SecurityLevel.AUTHOR, SecurityLevel.NONE });
		permissions.setColors(new Color[] { Color.red, Color.green });


		// Create the permission info panel.
		infoPanel = new JTextArea();
		infoPanel.setLineWrap(true);
		infoPanel.setWrapStyleWord(true);
		infoPanel.setEditable(false);
		infoPanel.setText("");
		infoPanel.setEditable(false);
		infoPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoPanel.setOpaque(false);


		// A listener will change the contents and visibility of the info panel.
		permissions.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				JPermissionTree.Permission p = permissions.getSelection();
				String info = (p == null) ? "" : p.info + "\n\n" + SecurityLevel.interpret(p.level);
				infoPanel.setText(info);
			}
		});

		JPanel pnlPermissions = new JPanel(new BorderLayout());
		pnlPermissions.add(permissions, BorderLayout.LINE_START);
		pnlPermissions.add(infoPanel, BorderLayout.CENTER);


		pnlPermissions.setBorder(BorderFactory.createTitledBorder("Permissions"));
		propertiesPanel.add(pnlHeader, BorderLayout.PAGE_START);
		propertiesPanel.add(pnlPermissions, BorderLayout.CENTER);


		return propertiesPanel;
	}


	/**Saves the permissions to the given State.  All other aspects of state should be 
	 * up-to-date.*/
	public void save() {
		HashMap<String, SecurityLevel> p = permissions.getPermissionMap();
		state.permissions.clear();
		for (Entry<String, SecurityLevel> entry : p.entrySet())
			state.permissions.put(entry.getKey(), entry.getValue());
	}
}
