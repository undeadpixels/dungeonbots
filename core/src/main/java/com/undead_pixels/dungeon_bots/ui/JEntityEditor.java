package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Sign;
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

	private JScriptCollectionControl scriptEditor = null;
	private JSignEditor signEditor;
	private JEntityPropertyControl propertyControl = null;


	/**@param security The level at which the editor will be created.  For example, if the security level 
	 * of the REPL requires "AUTHOR", but this is set up with "DEFAULT", a REPL will not appear in this editor.*/
	private JEntityEditor(Entity entity, SecurityLevel security, boolean transparent) {
		this.entity = entity;
		this.security = security;
		state = State.fromEntity(entity);

		// Set up the sign editor.
		if (entity instanceof Sign) {
			signEditor = new JSignEditor(state, (Sign) entity, security);
			addTab("Sign Text", null, signEditor, "The text this sign will show.");
		}

		// Set up the REPL.
		if (entity.getPermission(Entity.PERMISSION_COMMAND_LINE).level <= security.level) {
			JCodeREPL repl = new JCodeREPL(entity, transparent);
			addTab("Command Line", null, repl, "Instantaneous script runner.");
		}

		// Set up the script editor.
		if (entity.getPermission(Entity.PERMISSION_SCRIPT_EDITOR).level <= security.level) {
			scriptEditor = new JScriptCollectionControl(entity.getSandbox(), state, security);
			addTab("Scripts", null, scriptEditor, "Scripts relating to this entity.");
		}

		// Set up the properties tab.
		if (entity.getPermission(Entity.PERMISSION_PROPERTIES_EDITOR).level <= security.level) {
			propertyControl = new JEntityPropertyControl(state);
			addTab("Properties", null, propertyControl.create(), "Properties of this entity.");
		}

		this.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				String selectedTitle = JEntityEditor.this.getTitleAt(JEntityEditor.this.getSelectedIndex());
				entity.enqueueScript("onExamined", "message=" + selectedTitle);
			}
		});
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
		if (value) {
			UIBuilder.playSound("sounds/fordps3_boop.wav");
			Entity e = this.entity;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					e.enqueueScript("onExamined", "message=Entity Editor");
				}
			});

		}
	}


	public Entity getEntity() {
		return this.entity;
	}


	/**A handy data collection embodying the edited state of an editor.  To write the state 
	 * to an entity, call writeToEntity(entity).*/
	static final class State {

		final Entity entity;
		// private HashMap<String, Object> map = new HashMap<String, Object>();
		String name;
		final UserScriptCollection scripts;
		String help;
		final HashMap<String, SecurityLevel> permissions;
		Image image;
		Point2D.Float position;
		String signText;


		private State(Entity entity) {
			this.entity = entity;
			this.scripts = entity.getScripts().copy();
			this.permissions = entity.getPermissions();
			this.name = entity.getName();
			this.help = entity.getHelp();
			this.image = entity.getImage();
			this.position = entity.getPosition();

		}


		public static State fromEntity(Entity entity) {
			State s = new State(entity);
			return s;
		}


		/**Writes the given state to the entity.  Returns true if the write is successful, otherwise 
		 * returns false.*/
		public boolean writeToEntity(Entity entity) {

			entity.setName(name);

			if (this.scripts != null)
				entity.getScripts().setTo(scripts);

			entity.setHelp((this.help == null) ? "" : this.help);

			if (this.permissions != null)
				entity.setPermissions(this.permissions);


			// TODO: write image to Entity?

			// TODO: write position to Entity?

			return true;
		}


	}


	// ===================================================
	// ========= JEntityEditor help stuff ================
	// ===================================================


	/**Only one help frame can be open at the same time.*/
	private JFrame _OpenHelpFrame = null;


	/**Shows the help frame, if there is not one showing already.*/
	private void showHelp() {
		if (_OpenHelpFrame != null) {
			_OpenHelpFrame.requestFocus();
			return;
		}

		final JTextArea textPane = new JTextArea();
		textPane.setEditable(JEntityEditor.this.security.level >= JEntityEditor.this.entity
				.getPermission(Entity.PERMISSION_EDIT_HELP).level);
		textPane.setLineWrap(true);
		textPane.setWrapStyleWord(true);
		textPane.setText(state.help == null ? "" : state.help);

		_OpenHelpFrame = new JFrame();
		_OpenHelpFrame.setMinimumSize(new Dimension(300, 400));
		_OpenHelpFrame.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
		_OpenHelpFrame.setAlwaysOnTop(true);
		_OpenHelpFrame.setLocationRelativeTo(this);
		_OpenHelpFrame.setLocation(this.getWidth(), 0);
		_OpenHelpFrame.setTitle("Help");
		_OpenHelpFrame.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				if (security.level >= JEntityEditor.this.entity.getPermission(Entity.PERMISSION_EDIT_HELP).level)
					state.help = textPane.getText();
				_OpenHelpFrame = null;
			}
		});
		_OpenHelpFrame.add(new JScrollPane(textPane));
		_OpenHelpFrame.setVisible(true);
	}


	// ==================================================
	// ====== JEntityEditor dialog STUFF ================
	// ==================================================

	public static JEntityEditor createEntityEditorPane(Entity entity, SecurityLevel securityLevel, Container addTo,
			WorldView view, boolean transparent) {


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
		JEntityEditor jee = new JEntityEditor(entity, securityLevel, transparent);

		if (jee.getTabCount() == 0) // Security allow any editing?
			return null;


		// The dialog will handle commit/cancel. It packages up and pushes its
		// own Undoable.
		ActionListener dialogController = new DialogController(addTo, jee, entity, view);

		JPanel pnlButtons = new JPanel(new HorizontalLayout());
		if (transparent)
			pnlButtons.setOpaque(false);
		pnlButtons.add(UIBuilder.buildButton().image("icons/ok.png").toolTip("Approve changes and close the dialog.")
				.action("COMMIT", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/close.png").toolTip("Cancel changes and close the dialog.")
				.action("CANCEL", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/zoom.png").toolTip("Set view to center.")
				.action("CENTER_VIEW", dialogController).border(new EmptyBorder(10, 10, 10, 10)).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/question.png").toolTip("Open help regarding this entity.")
				.action("HELP", dialogController).create());


		addTo.setLayout(new BorderLayout());
		addTo.add(jee, BorderLayout.CENTER);
		addTo.add(pnlButtons, BorderLayout.SOUTH);


		return jee;
	}


	/**Returns null if security will not allow any editing of this entity.*/
	public static JEntityEditor createDialog(java.awt.Window owner, Entity entity, String title,
			SecurityLevel securityLevel, WorldView view) {

		// Check if there's already an open editor for this entity. If so, just
		// return that editor.
		if (openEditors.containsKey(entity)) {
			JEntityEditor existing = openEditors.get(entity);
			existing.requestFocus();
			return existing;
		}

		// Build the dialog.
		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		JEntityEditor jee = createEntityEditorPane(entity, securityLevel, dialog, view, false);
		
		// Create the dialog that contains the editor.
		openEditors.put(entity, jee);
		jee.dialog = dialog;


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

		dialog.setSize(600, 500);

		return jee;
	}


	private static class DialogController implements ActionListener {

		private final JEntityEditor jee;
		private final Component dialog;
		private final Entity entity;
		private final WorldView view;


		public DialogController(Component dialog, JEntityEditor jee, Entity entity, WorldView view) {
			this.dialog = dialog;
			this.jee = jee;
			this.entity = entity;
			this.view = view;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "COMMIT":
				if (jee.scriptEditor != null)
					jee.scriptEditor.save();
				if (jee.signEditor != null)
					jee.signEditor.save();
				if (jee.propertyControl != null)
					jee.propertyControl.save();
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
				UIBuilder.playSound("sounds/dland_approve.wav");
				Tool.pushUndo(entity.getWorld(), u);
				if (dialog instanceof JDialog)
					((JDialog) dialog).dispose();
				jee.dialog = null;
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						entity.enqueueScript("onEdited");
					}

				});
				break;
			case "CANCEL":
				if (jee.changed) {
					int confirm = JOptionPane.showConfirmDialog(jee, "Discard all changes?", "Confirm",
							JOptionPane.YES_NO_OPTION);
					if (confirm != JOptionPane.YES_OPTION)
						break;
				}
				jee.dialog = null;
				if (dialog instanceof JDialog)
					((JDialog) dialog).dispose();
				UIBuilder.playSound("sounds/deathscyp_error.wav");
				break;
			case "CENTER_VIEW":
				Point2D.Float pt = entity.getPosition();
				view.getCamera().setPosition(pt.x, pt.y);
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
