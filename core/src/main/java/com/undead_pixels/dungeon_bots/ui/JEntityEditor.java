package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;

/**
 * A GUI object whose purpose is to give users a way to change the contents of
 * an entity, including any associated scripts.
 */
public final class JEntityEditor extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final HashMap<Entity, JDialog> _OpenEditors = new HashMap<Entity, JDialog>();

	/**
	 * Creates a new entity editor in a modeless dialog. Only one such dialog
	 * per entity is allowed. If one is already open, that dialog will request
	 * focus and the method returns null. Otherwise, returns the newly-created
	 * editor.
	 */
	public static JEntityEditor create(java.awt.Window owner, Entity entity, SecurityLevel securityLevel,
			String title) {

		if (_OpenEditors.containsKey(entity)) {
			System.err.println("An editor is already open for this entity:  " + entity.toString());
			JDialog dialog = _OpenEditors.get(entity);
			dialog.requestFocus();
			return null;
		}

		JEntityEditor jee = new JEntityEditor(entity, securityLevel);
		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		dialog.add(jee);
		dialog.pack();
		dialog.addWindowListener(new WindowListenerAdapter() {
			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				_OpenEditors.remove(entity);
			}
		});
		dialog.setVisible(true);
		_OpenEditors.put(entity, dialog);
		return jee;
	}

	private JEntityEditor(Entity entity, SecurityLevel securityLevel) {
		super(new BorderLayout());

		JCodeREPL repl = new JCodeREPL(entity);
		
		JComponent scriptPanel = new JCodeEditorPaneController(entity, securityLevel).create();
		JComponent propertiesPanel = new JEntityPropertyTab(entity, securityLevel).create();

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Command Line", null, repl, "Instantaneous script runner.");	
		tabPane.addTab("Scripts", null, scriptPanel, "Scripts relating to this entity.");
		tabPane.addTab("Properties", propertiesPanel);

		this.add(tabPane, BorderLayout.LINE_START);

	}

}
