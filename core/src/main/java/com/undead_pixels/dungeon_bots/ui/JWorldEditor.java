package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;

/**Like a JEntityEditor, adapted quick-and-dirty for editing a World.*/
public class JWorldEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**Only one dialog at a time can be opened.*/
	private static JDialog openDialog = null;
	private final World _World;
	private final DefaultListModel<UserScript> _EditedScripts;

	HashMap<String, Object> _OriginalState;
	private Undoable.Listener _UndoableListener;
	private final JCodeEditorPaneController _ScriptEditor;
	private final Controller _Controller;


	public static JWorldEditor create(java.awt.Window owner, World world, String title,
			Undoable.Listener undoableListener) {

		if (openDialog != null) {
			System.err.println("An editor is already open.  Only one can be opened at a time.");
			openDialog.requestFocus();
			return null;
		}

		JWorldEditor jwe = new JWorldEditor(world);
		jwe._UndoableListener = undoableListener;

		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		dialog.add(jwe);
		dialog.pack();
		dialog.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				openDialog = null;
			}
		});
		openDialog = dialog;
		dialog.setVisible(true);

		return jwe;
	}


	public JWorldEditor(World world) {
		this._World = world;
		this._Controller = new Controller();
		JCodeREPL repl = new JCodeREPL(world);
		repl.addActionListener(_Controller);
		_ScriptEditor = new JCodeEditorPaneController(_World, SecurityLevel.AUTHOR);
		JComponent scriptPanel = _ScriptEditor.create();

		_OriginalState = readWorld(world);
		_EditedScripts = new DefaultListModel<UserScript>();
		for (UserScript u : world.getScripts().toArray())
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
		tabPane.addTab("Scripts", null, scriptPanel);

		this.setLayout(new HorizontalLayout());
		this.add(tabPane, BorderLayout.LINE_START);
	}


	public void reset() {
		UserScript[] originalScripts = (UserScript[]) _OriginalState.get("scripts");
		_EditedScripts.clear();
		for (UserScript u : originalScripts)
			_EditedScripts.addElement(u);
	}


	public void save() {

		// Step #1 - validate that the world hasn't been changed while the
		// editor was open.
		HashMap<String, Object> currentState = readWorld(_World);
		assert currentState.size() == _OriginalState.size();
		UserScript[] originalScripts = (UserScript[]) _OriginalState.get("scripts");
		UserScript[] currentScripts = (UserScript[]) currentState.get("scripts");
		if (originalScripts.length != currentScripts.length)
			throw new RuntimeException(
					"Scripts were added to ro removed from the World's script list while the World Editor was open.");
		for (int i = 0; i < originalScripts.length; i++) {
			if (!originalScripts[i].equals(currentScripts[i]))
				throw new RuntimeException("Scripts were changed while the World Editor was open.");
		}

		// Step #2 - do the save
		UserScript[] newScripts = new UserScript[_EditedScripts.size()];
		for (int i = 0; i < newScripts.length; i++)
			newScripts[i] = _EditedScripts.getElementAt(i);
		writeWorld(_World, newScripts);

		// Step #3 - signal that an undoable change has happened.
		if (_UndoableListener != null) {
			HashMap<String, Object> newState = new HashMap<String, Object>();
			newState.put("world", _World);
			newState.put("scripts", newScripts);
			Undoable<HashMap<String, Object>> u = new Undoable<HashMap<String, Object>>(_OriginalState, newState) {

				@Override
				protected void undoValidated() {
					World w = (World) before.get("world");
					UserScript[] beforeScripts = (UserScript[]) before.get("scripts");
					writeWorld(w, beforeScripts);
				}


				@Override
				protected void redoValidated() {
					World w = (World) after.get("world");
					UserScript[] afterScripts = (UserScript[]) after.get("scripts");
					writeWorld(w, afterScripts);
				}

			};
			_UndoableListener.pushUndoable(u);
		}

		// Step #4 - the present original state is now the world's current
		// state.
		_OriginalState = readWorld(_World);
	}


	private HashMap<String, Object> readWorld(World w) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("world", w);
		result.put("scripts", w.getScripts().toArray());
		return result;
	}


	/**Writes the given statistics to the specified entity.  This method must be static because it may be 
	 * called by an Undoable when a reference to _Entity has expired.*/
	private static void writeWorld(World world, UserScript[] scripts) {
		world.setScripts(scripts);

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


	private class Controller implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()){
			default:
				System.out.println(this.getClass().getName() + " has not implemented command " + e.getActionCommand());
			}
			
		}

	}

}
