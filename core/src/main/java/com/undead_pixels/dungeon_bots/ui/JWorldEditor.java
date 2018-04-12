package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;
import com.undead_pixels.dungeon_bots.ui.screens.Tool;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;

/**Like a JEntityEditor, adapted quick-and-dirty for editing a World.*/
@SuppressWarnings("serial")
public class JWorldEditor extends JTabbedPane {

	private static final HashMap<World, JWorldEditor> openEditors = new HashMap<World, JWorldEditor>();

	private final World world;
	private SecurityLevel security;
	private final State state;
	private JDialog dialog = null;
	private boolean changed = false;

	private JScriptCollectionControl scriptEditor = null;


	/**@param security The level at which the editor will be created.  For example, if the security level 
	 * of the REPL requires "AUTHOR", but this is set up with "DEFAULT", a REPL will not appear in this editor.*/
	private JWorldEditor(World world, SecurityLevel security) {
		this.world = world;
		this.security = security;
		state = State.fromWorld(world);

		// Set up the REPL.
		if (world.getPermission("REPL").level <= security.level) {
			JCodeREPL repl = new JCodeREPL(world);
			addTab("Command Line", null, repl, "Instantaneous script runner.");
		}

		// Set up the script editor.
		if (world.getPermission("SCRIPT_EDITOR").level <= security.level) {
			scriptEditor = new JScriptCollectionControl(state, security);
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


	public World getWorld() {
		return this.world;
	}


	/**A handy data collection embodying the edited state of an editor.  To write the state 
	 * to an entity, call writeToEntity(entity).*/
	static final class State {

	
 final UserScriptCollection scripts;

		private State(UserScriptCollection scripts) {
			this.scripts = scripts;
		}


		public static State fromWorld(World world) {
			State s = new State(world.getScripts().copy());			
			return s;
		}

		/**Writes the given state to the world.*/
		public void writeToWorld(World world) {
			world.getScripts().setTo(scripts);
		}
	}


	// ==================================================
	// ====== JEntityEditor dialog STUFF ================
	// ==================================================

	/**Returns null if security will not allow any editing of this entity.*/
	public static JWorldEditor createDialog(java.awt.Window owner, World world, String title,
			SecurityLevel securityLevel) {

		// If there's already an open editor for this entity, don't allow
		// another dialog.
		JWorldEditor existing = openEditors.get(world);
		if (existing != null) {
			if (existing.dialog != null)
				existing.dialog.requestFocus();
			else
				existing.requestFocus();
			return existing;
		}

		// Create the editor.
		JWorldEditor jwe = new JWorldEditor(world, securityLevel);

		if (jwe.getTabCount() == 0) // Security allow any editing?
			return null;

		// Create the dialog that contains the editor.
		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		openEditors.put(world, jwe);
		jwe.dialog = dialog;
		dialog.setLayout(new BorderLayout());
		dialog.add(jwe, BorderLayout.CENTER);


		dialog.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				openEditors.remove(world);
			}
		});

		// The dialog will handle commit/cancel. It packages up and pushes its
		// own Undoable.
		ActionListener dialogController = new DialogController(dialog, jwe, world);

		JPanel pnlButtons = new JPanel(new HorizontalLayout());
		pnlButtons.add(UIBuilder.buildButton().image("icons/ok.png").toolTip("Approve changes and close the dialog.")
				.action("COMMIT", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/cancel.png").toolTip("Cancel changes and close the dialog.")
				.action("CANCEL", dialogController).create());
		dialog.add(pnlButtons, BorderLayout.PAGE_END);
		dialog.pack();
		return jwe;
	}


	private static class DialogController implements ActionListener {

		private final JWorldEditor jwe;
		private final JDialog dialog;
		private final World world;


		public DialogController(JDialog dialog, JWorldEditor jwe, World world) {
			this.dialog = dialog;
			this.jwe = jwe;
			this.world = world;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "COMMIT":
				jwe.scriptEditor.save();
				Undoable<State> u = new Undoable<State>(State.fromWorld(world), jwe.state) {

					@Override
					protected void undoValidated() {
						before.writeToWorld(world);
					}


					@Override
					protected void redoValidated() {
						after.writeToWorld(world);
					}

				};
				jwe.state.writeToWorld(world);
				Tool.pushUndo(world, u);
				dialog.dispose();
				jwe.dialog = null;
				break;
			case "CANCEL":
				if (jwe.changed) {
					int confirm = JOptionPane.showConfirmDialog(jwe, "Discard all changes?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (confirm != JOptionPane.YES_OPTION)
						break;
				}
				jwe.dialog = null;
				dialog.dispose();
				break;
			default:
				throw new RuntimeException("Not implemented command " + e.getActionCommand());
			}
		}
	}


}
