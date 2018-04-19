/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
	private final ArrayList<CheckboxController> checkboxes = new ArrayList<>();
	private final State state;
	
	private JComponent lazyCreated;


	public JEntityPropertyControl(State state) {
		this.entity = state.entity;
		this.state = state;
		
		entity.getWorld().listenTo(EntityEventType.ENTITY_MOVED, this, (e) -> updateBorderName());
	}
	
	private void updateBorderName() {
		if(lazyCreated != null) {
			lazyCreated.setBorder(BorderFactory.createTitledBorder(state.name+" @ "+"("+entity.getPosition().x+", "+entity.getPosition().y+")"));
		}
	}


	public JComponent create() {
		if(lazyCreated != null) {
			return lazyCreated;
		}
		Box propertiesPanel = new Box(BoxLayout.Y_AXIS);
		propertiesPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		Box topBox = new Box(BoxLayout.X_AXIS);
		
		JLabel imgLabel = new JLabel(new ImageIcon(entity.getImage().getScaledInstance(64, 64, BufferedImage.SCALE_FAST)));
		imgLabel.setAlignmentX(0.0f);
		imgLabel.setHorizontalAlignment(JLabel.LEFT);
		topBox.add(imgLabel);
		topBox.add(new JPanel()); // spacing
		
		
		JTextField fldName = new JTextField(10);
		fldName.setText(entity.getName());
		fldName.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				state.name = fldName.getText();
				updateBorderName();
				
				Window root = SwingUtilities.getWindowAncestor(lazyCreated);
				
				if(root instanceof JDialog) {
					((JDialog) root).setTitle(fldName.getText());
				}
			}
			
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				changedUpdate(arg0);
			}
			
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				changedUpdate(arg0);
			}
		});
		fldName.setMaximumSize(fldName.getPreferredSize());
		
		Box nameBox = new Box(BoxLayout.X_AXIS);
		nameBox.setAlignmentX(1.0f);
		nameBox.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		nameBox.add(new JPanel());
		nameBox.add(new JLabel("Name:"));
		nameBox.add(fldName);
		
		Box nameBoxWrapper = new Box(BoxLayout.Y_AXIS);
		nameBoxWrapper.add(Box.createGlue());
		nameBoxWrapper.add(nameBox);
		nameBoxWrapper.add(Box.createGlue());
		topBox.add(nameBoxWrapper);
		topBox.setMaximumSize(new Dimension(99999, 120));
		
		
		
		propertiesPanel.add(topBox);
		
		JSeparator separator = new JSeparator();
		separator.setMaximumSize(new Dimension(99999, 15));
		propertiesPanel.add(separator);
		
		
//		Entity.PERMISSION_SELECTION
//		Entity.PERMISSION_ENTITY_EDITOR
//		Entity.PERMISSION_COMMAND_LINE
//		Entity.PERMISSION_SCRIPT_EDITOR
//		Entity.PERMISSION_ADD_REMOVE_SCRIPTS
//		Entity.PERMISSION_PROPERTIES_EDITOR
//		Entity.PERMISSION_EDIT_HELP "Access level for editing the help info for an entity."
		
		checkboxes.add(new CheckboxController("Selection", Entity.PERMISSION_SELECTION,
				"Access to whether the entity can be selected. Note that if a user cannot select the entity, the entity editor cannot be opened either."));
		checkboxes.add(new CheckboxController("Entity editor", Entity.PERMISSION_ENTITY_EDITOR,
				"Access to the entity editing dialog (the dialog you're looking at right now)."));
		checkboxes.add(new CheckboxController("Command line", Entity.PERMISSION_COMMAND_LINE,
				"Access level for the command line in the entity editing dialog."));
		checkboxes.add(new CheckboxController("Script editor", Entity.PERMISSION_SCRIPT_EDITOR,
				"Access level for the script editor in the entity editing dialog."));
		checkboxes.add(new CheckboxController("Properties editor", Entity.PERMISSION_PROPERTIES_EDITOR,
				"Access to the property editor in the entity editing dialog (you're looking at the property editor right now)."));

		
		for(CheckboxController c : checkboxes) {
			propertiesPanel.add(c.makeCheckbox());
		}
		
		//propertiesPanel.add(new JPanel()); // spacing
		
		JScrollPane scroller = new JScrollPane(propertiesPanel);
		scroller.setAlignmentX(0.0f);

		lazyCreated = scroller;
		updateBorderName();

		return lazyCreated;
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
			checkBox.setHorizontalAlignment(SwingConstants.LEFT);
			checkBox.setAlignmentX(JCheckBox.LEFT_ALIGNMENT);
			
			Box ret = new Box(BoxLayout.X_AXIS);
			ret.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			ret.add(checkBox);
			ret.add(Box.createGlue());
			
			ret.setBorder(BorderFactory.createEmptyBorder());
			
			ret.setMaximumSize(new Dimension(9999, checkBox.getPreferredSize().height));
			
			return ret;
			
		}

		/**
		 * @param state 
		 */
		public void save (State state) {
			state.permissions.put(flagName, checkBox.isSelected() ? SecurityLevel.NONE : SecurityLevel.AUTHOR);
		}
	}

	/**
	 * @param state 
	 * 
	 */
	public void save () {
		for(CheckboxController c: checkboxes) {
			c.save(state);
		}
	}
}