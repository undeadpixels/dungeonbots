package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;
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

	// private final DefaultListModel<UserScript> _ScriptList;
	private final Entity _Entity;
	private final Controller _Controller;
	private SecurityLevel _SecurityLevel;
	private JButton _BttnRemoveScript; // A reference is kept because the button
										// should be disabled when no script is
										// selected.
	private DefaultListModel<UserScript> _EditedScripts;
	private String _EditedHelp;

	/**The original state of the Entity.  Used to detect if something else changed the Entity
	 * while this Entity Editor is open.*/
	private HashMap<String, Object> _OriginalState;

	private Undoable.Listener _UndoableListener = null;
	private JCodeEditorPaneController _ScriptEditor;


	/**
	 * Creates a new entity editor in a modeless dialog. Only one such dialog
	 * per entity is allowed. If one is already open, that dialog will request
	 * focus and the method returns null. Otherwise, returns the newly-created
	 * editor.
	 */
	public static JEntityEditor create(java.awt.Window owner, Entity entity, SecurityLevel securityLevel, String title,
			Undoable.Listener undoableListener) {

		if (_OpenEditors.containsKey(entity)) {
			//System.err.println("An editor is already open for this entity:  " + entity.toString());
			JDialog dialog = _OpenEditors.get(entity);
			dialog.requestFocus();
			return null;
		}

		JEntityEditor jee = new JEntityEditor(entity, securityLevel);
		jee._UndoableListener = undoableListener;

		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		dialog.add(jee);
		dialog.pack();
		dialog.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				// WINDOW_FIRST=200
				// WINDOW_OPENED=200
				// WINDOW_CLOSING=201
				// WINDOW_CLOSED=202
				// WINDOW_ICONIFIED=203
				// WINDOW_DEICONIFIED=204
				// WINDOW_ACTIVATED=205
				// WINDOW_DEACTIVATED=206
				// WINDOW_GAINED_FOCUS=207
				// WINDOW_LOST_FOCUS=208
				// WINDOW_STATE_CHANGED=209
				// WINDOW_LAST=209				
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				_OpenEditors.remove(entity);
				if (jee != null && jee._CurrentHelpFrame != null) {
					jee._CurrentHelpFrame.dispose();
				}
			}
		});
		dialog.setVisible(true);
		_OpenEditors.put(entity, dialog);
		return jee;
	}


	private JEntityEditor(Entity entity, SecurityLevel securityLevel) {
		super(new BorderLayout());

		_Entity = entity;
		_Controller = new Controller();
		_SecurityLevel = securityLevel;

		JCodeREPL repl = new JCodeREPL(entity);
		repl.addActionListener(_Controller);
		_ScriptEditor = new JCodeEditorPaneController(entity, securityLevel);
		JComponent scriptPanel = _ScriptEditor.create();
		JComponent propertiesPanel = new JEntityPropertyTab(entity, securityLevel).create();

		// Peg the entity's original state to detect changes while the editor is
		// open.
		_OriginalState = readEntity(entity);
		_EditedHelp = entity.help;
		_EditedScripts = new DefaultListModel<UserScript>();
		for (UserScript u : entity.getScripts().toArray())
			_EditedScripts.addElement(u);


		JPanel bttnBottomPanel = new JPanel();
		bttnBottomPanel.setLayout(new GridLayout(1, 3, 10, 10));
		JButton bttnReset = UIBuilder.buildButton().text("Reset").toolTip("Reset the entity characteristics.")
				.action("RESET", _Controller).create();
		JButton bttnOK = UIBuilder.buildButton().text("Save").toolTip("Approve the changes.")
				.action("SAVE", _Controller).create();
		JButton bttnClose = UIBuilder.buildButton().text("Close").toolTip("Close the editor.")
				.action("CLOSE", _Controller).create();
		bttnBottomPanel.add(bttnReset);
		bttnBottomPanel.add(bttnOK);
		// TODO: Should there be a close button? Only if this script editor is
		// in its
		// own dialog. But, I won't know that until after construction time.
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// Invoke later because there will be no containing parent at
				// construction time.
				if (getContainingDialog() != null)
					bttnBottomPanel.add(bttnClose);
			}
		});


		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Command Line", null, repl, "Instantaneous script runner.");
		tabPane.addTab("Scripts", null, scriptPanel, "Scripts relating to this entity.");
		tabPane.addTab("Properties", propertiesPanel);


		this.setLayout(new HorizontalLayout());
		this.add(tabPane, BorderLayout.LINE_START);


	}


	/**Reads the entity for populating the Editor in the beginning, and for validating at save time.
	 * NOTE:  better to just set up the hash map in line?  Whatever is more readable.*/
	@Deprecated
	private HashMap<String, Object> readEntity(Entity e) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("entity", e);
		result.put("help", e.help);
		result.put("scripts", e.getScripts().toArray());
		return result;
	}


	/** Resets all characteristics of this entity to the original state. */
	public void reset() {
		_EditedHelp = new String((String) _OriginalState.get("help"));
		UserScript[] originalScripts = (UserScript[]) _OriginalState.get("scripts");
		_EditedScripts.clear();
		for (UserScript u : originalScripts) {
			_EditedScripts.addElement(u);
		}
	}


	/**
	 * Saves all characteristics to the Entity as they are presented in this
	 * GUI.
	 */
	public void save() {

		// Step #1 - validate the Entity is still in the same state. With the
		// JEntityEditor non-blocking, it is possible for other changes to the
		// entity to be made while the Editor is open. If it has changed, the
		// editor should properly indicate an error.
		HashMap<String, Object> currentState = readEntity(_Entity);
		assert currentState.size() == _OriginalState.size(); // sanity check
		if (!_OriginalState.get("help").equals(currentState.get("help")))
			throw new RuntimeException("Entity help string was changed while Entity Editor was open.");
		UserScript[] originalScripts = (UserScript[]) _OriginalState.get("scripts");
		UserScript[] currentScripts = (UserScript[]) currentState.get("scripts");
		if (originalScripts.length != currentScripts.length)
			throw new RuntimeException(
					"Scripts were added to or removed from the Entity's script list while the Entity Editor was open.");
		for (int i = 0; i < originalScripts.length; i++) {
			if (!originalScripts[i].equals(currentScripts[i]))
				throw new RuntimeException("Scripts were changed while the Entity Editor was open.");
		}


		// Step #2 - now, having gotten through validation, do the save.
		UserScript[] newScripts = new UserScript[_EditedScripts.size()];
		for (int i = 0; i < newScripts.length; i++)
			newScripts[i] = _EditedScripts.getElementAt(i);
		String newHelp = _EditedHelp;
		writeEntity(_Entity, newScripts, newHelp);

		// Step #3 - signal that an undoable change has happened.
		if (_UndoableListener != null) {
			HashMap<String, Object> newState = new HashMap<String, Object>();
			newState.put("entity", _Entity);
			newState.put("scripts", newScripts);
			newState.put("help", newHelp);
			Undoable<HashMap<String, Object>> u = new Undoable<HashMap<String, Object>>(_OriginalState, newState) {

				@Override
				protected void undoValidated() {
					Entity e = (Entity) before.get("Entity");
					UserScript[] beforeScripts = (UserScript[]) before.get("scripts");
					String beforeHelp = (String) before.get("help");
					writeEntity(e, beforeScripts, beforeHelp);
				}


				@Override
				protected void redoValidated() {
					Entity e = (Entity) after.get("Entity");
					UserScript[] afterScripts = (UserScript[]) after.get("scripts");
					String afterHelp = (String) after.get("help");
					writeEntity(e, afterScripts, afterHelp);
				}

			};
			_UndoableListener.pushUndoable(u);
		}

		// Step #4 - the present original state is now the entity's current
		// state.
		_OriginalState = readEntity(_Entity);
	}


	/**Writes the given statistics to the specified entity.  This method must be static because it may be 
	 * called by an Undoable when a reference to _Entity has expired.*/
	private static void writeEntity(Entity entity, UserScript[] scripts, String help) {
		entity.setScripts(scripts);
		entity.help = help;
	}


	// ===================================================
	// ========= JEntityEditor help stuff ================
	// ===================================================


	private JFrame _CurrentHelpFrame = null;
	private Document _CurrentHelpDocument = null;


	/**Shows the help frame, if there is not one showing already.*/
	private void showHelp() {
		if (_CurrentHelpFrame != null)
			return;
		_CurrentHelpFrame = new JFrame();
		_CurrentHelpFrame.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
		JEditorPane textPane = new JEditorPane();
		SecurityLevel securityLevel = JEntityEditor.this._SecurityLevel;
		textPane.setEditable(securityLevel.level >= SecurityLevel.AUTHOR.level);
		textPane.setText(_EditedHelp);
		_CurrentHelpDocument = textPane.getDocument();
		JScrollPane scroller = new JScrollPane(textPane);
		scroller.setPreferredSize(new Dimension(400, this.getHeight()));
		_CurrentHelpFrame.add(scroller);
		_CurrentHelpFrame.setAlwaysOnTop(true);
		_CurrentHelpFrame.pack();
		_CurrentHelpFrame.setLocationRelativeTo(this);
		_CurrentHelpFrame.setLocation(this.getWidth(), 0);
		_CurrentHelpFrame.setTitle("Help");
		_CurrentHelpFrame.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				try {
					if (_SecurityLevel.level >= SecurityLevel.AUTHOR.level)
						_EditedHelp = _CurrentHelpDocument.getText(0, _CurrentHelpDocument.getLength());
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				_CurrentHelpFrame = null;
				_CurrentHelpDocument = null;
			}
		});
		_CurrentHelpFrame.setVisible(true);
	}


	/**
	 * Used to find the containing dialog parent, if there is one. A
	 * JEntityEditor GUI that exists within a JDialog behaves differently from
	 * one who doesn't.
	 */
	private JDialog getContainingDialog() {
		Container parent = this.getParent();
		while (parent != null) {
			if (parent instanceof JDialog)
				return (JDialog) parent;
			parent = parent.getParent();
		}
		return null;
	}


	// ===================================================
	// ========= JEntityEditor CONTROL ==================
	// ===================================================

	/** The controller for this editor. */
	private class Controller implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "HELP":
				showHelp();
				return;
			default:
				System.out.println("JEntityEditor has not implemented command " + e.getActionCommand());
				return;
			}
		}
	}


}
