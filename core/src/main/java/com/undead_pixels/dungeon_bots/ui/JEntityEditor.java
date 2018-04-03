package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;
import com.undead_pixels.dungeon_bots.ui.screens.Tool;

/**
 * A GUI object whose purpose is to give users a way to change the contents of
 * an entity, including any associated scripts.
 */
@SuppressWarnings("serial")
public final class JEntityEditor extends JTabbedPane {

	private static final HashMap<Entity, JEntityEditor> openEditors = new HashMap<Entity, JEntityEditor>();

	private final Entity entity;
	private SecurityLevel security;
	private final State state;
	private JDialog dialog = null;
	private boolean changed = false;

	private JScriptCollectionEditor scriptEditor = null;


	/**@param security The level at which the editor will be created.  For example, if the security level 
	 * of the REPL requires "AUTHOR", but this is set up with "DEFAULT", a REPL will not appear in this editor.*/
	private JEntityEditor(Entity entity, SecurityLevel security) {
		this.entity = entity;
		this.security = security;
		state = State.fromEntity(entity);

		// Set up the REPL.
		if (entity.getPermission("REPL").level <= security.level) {
			JCodeREPL repl = new JCodeREPL(entity);
			addTab("Command Line", null, repl, "Instantaneous script runner.");
		}

		// Set up the script editor.
		if (entity.getPermission("SCRIPT_EDITOR").level <= security.level) {
			scriptEditor = new JScriptCollectionEditor(state.getScripts(), security);
			addTab("Scripts", null, scriptEditor, "Scripts relating to this entity.");
		}

	}


	/**Sets editor visibility.  If the editor is associated with a dialog, sets the dialog visibility.*/
	@Override
	public void setVisible(boolean value) {
		if (dialog != null) {
			dialog.setVisible(value);
			super.setVisible(value);
		}

		else
			super.setVisible(value);
	}


	public Entity getEntity() {
		return this.entity;
	}


	/**A handy data collection embodying the edited state of an editor.  To write the state 
	 * to an entity, call writeToEntity(entity).*/
	public static final class State {

		private HashMap<String, Object> map = new HashMap<String, Object>();


		private State() {
		}


		public static State fromEntity(Entity entity) {
			State s = new State();
			s.map.put("SCRIPTS", entity.getScripts().copy());
			s.map.put("HELP", entity.help);
			return s;
		}


		public UserScriptCollection getScripts() {
			return ((UserScriptCollection) map.get("SCRIPTS"));
		}


		/**Writes the given state to the entity.*/
		public void writeToEntity(Entity entity) {
			UserScriptCollection scripts = (UserScriptCollection) map.get("SCRIPTS");
			entity.getScripts().setTo(scripts);

			String help = (String) map.get("HELP");
			if (help != null)
				entity.help = help;
		}
	}


	// ===================================================
	// ========= JEntityEditor help stuff ================
	// ===================================================


	private JFrame _OpenHelpFrame = null;
	private Document _CurrentHelpDocument = null;


	/**Shows the help frame, if there is not one showing already.*/
	private void showHelp() {
		if (_OpenHelpFrame != null)
			return;
		_OpenHelpFrame = new JFrame();
		_OpenHelpFrame.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
		JEditorPane textPane = new JEditorPane();
		textPane.setEditable(
				JEntityEditor.this.security.level >= JEntityEditor.this.entity.getPermission("EDIT_HELP").level);
		String help = (String) state.map.get("HELP");
		if (help == null)
			help = "No help for this entity.";
		_CurrentHelpDocument = textPane.getDocument();
		JScrollPane scroller = new JScrollPane(textPane);
		// scroller.setPreferredSize(new Dimension(400, this.getHeight()));
		_OpenHelpFrame.add(scroller);
		_OpenHelpFrame.setAlwaysOnTop(true);
		_OpenHelpFrame.pack();
		_OpenHelpFrame.setLocationRelativeTo(this);
		_OpenHelpFrame.setLocation(this.getWidth(), 0);
		_OpenHelpFrame.setTitle("Help");
		_OpenHelpFrame.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				try {
					if (security.level >= SecurityLevel.AUTHOR.level)
						state.map.put("HELP", _CurrentHelpDocument.getText(0, _CurrentHelpDocument.getLength()));
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				_OpenHelpFrame = null;
				_CurrentHelpDocument = null;
			}
		});
		_OpenHelpFrame.setVisible(true);
	}


	// ==================================================
	// ====== JEntityEditor dialog STUFF ================
	// ==================================================

	/**Returns null if security will not allow any editing of this entity.*/
	public static JEntityEditor createDialog(java.awt.Window owner, Entity entity, String title,
			SecurityLevel securityLevel) {

		// If there's already an open editor for this entity, don't allow
		// another dialog.
		JEntityEditor existing = openEditors.get(entity);
		if (existing != null) {
			if (existing.dialog != null)
				existing.dialog.requestFocus();
			else
				existing.requestFocus();
			return existing;
		}

		// Create the editor.
		JEntityEditor jee = new JEntityEditor(entity, securityLevel);

		if (jee.getTabCount() == 0) // Security allow any editing?
			return null;

		// Create the dialog that contains the editor.
		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		openEditors.put(entity, jee);
		jee.dialog = dialog;
		dialog.setLayout(new BorderLayout());
		dialog.add(jee, BorderLayout.CENTER);


		// If a dialog is disposed, it should remove the entity from the
		// already-open dialog list, and dispose of any help frames so they're
		// not orphans.
		dialog.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				openEditors.remove(entity);
				if (jee._OpenHelpFrame != null)
					jee._OpenHelpFrame.dispose();

			}
		});

		// The dialog will handle commit/cancel. It packages up and pushes its
		// own Undoable.
		ActionListener dialogController = new DialogController(dialog, jee, entity);

		JPanel pnlButtons = new JPanel(new HorizontalLayout());
		pnlButtons.add(UIBuilder.buildButton().image("icons/ok.png").toolTip("Approve changes and close the dialog.")
				.action("COMMIT", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/cancel.png").toolTip("Cancel changes and close the dialog.")
				.action("CANCEL", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/question.png").toolTip("Open help regarding this entity.")
				.action("HELP", dialogController).create());
		dialog.add(pnlButtons, BorderLayout.PAGE_END);
		dialog.pack();
		return jee;
	}


	private static class DialogController implements ActionListener {

		private final JEntityEditor jee;
		private final JDialog dialog;
		private final Entity entity;


		public DialogController(JDialog dialog, JEntityEditor jee, Entity entity) {
			this.dialog = dialog;
			this.jee = jee;
			this.entity = entity;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "COMMIT":
				jee.scriptEditor.save();
				Undoable<State> u = new Undoable<State>(State.fromEntity(entity), jee.state) {

					@Override
					protected void undoValidated() {
						before.writeToEntity(entity);
					}


					@Override
					protected void redoValidated() {
						after.writeToEntity(entity);
					}

				};
				jee.state.writeToEntity(jee.entity);
				Tool.pushUndo(entity.getWorld(), u);
				dialog.dispose();
				jee.dialog = null;
				break;
			case "CANCEL":
				if (jee.changed) {
					int confirm = JOptionPane.showConfirmDialog(jee, "Discard all changes?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (confirm != JOptionPane.YES_OPTION)
						break;
				}
				jee.dialog = null;
				dialog.dispose();
				break;
			case "HELP":
				jee.showHelp();
				break;
			default:
				throw new RuntimeException("Not implemented command " + e.getActionCommand());
			}
		}
	}


}
