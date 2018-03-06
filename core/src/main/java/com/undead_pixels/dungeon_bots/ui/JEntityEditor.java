package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.code_edit.JScriptEditor;
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

	private final DefaultListModel<UserScript> _ScriptList;
	private final Entity _Entity;
	private final JScriptEditor _Editor;
	private final Controller _Controller;
	private SecurityLevel _SecurityLevel;
	private JTextPane _InstructionPane = null;


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
		_ScriptList = new DefaultListModel<UserScript>();
		_SecurityLevel = securityLevel;

		reset();

		JList<UserScript> scriptList = new JList<UserScript>(_ScriptList);
		scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptList.setLayoutOrientation(JList.VERTICAL);
		scriptList.setVisibleRowCount(-1);
		scriptList.addListSelectionListener(_Controller);
		JScrollPane scriptScroller = new JScrollPane(scriptList);
		scriptScroller.setPreferredSize(new Dimension(200, this.getSize().height - 100));
		scriptScroller.setBorder(BorderFactory.createTitledBorder("Scripts for this Entity"));

		JPanel bttnPanel = new JPanel();
		bttnPanel.setLayout(new GridLayout(1, 3, 10, 10));
		JButton bttnReset = UIBuilder.buildButton().text("Reset").toolTip("Reset the entity characteristics.")
				.action("RESET", _Controller).create();
		JButton bttnOK = UIBuilder.buildButton().text("Save").toolTip("Approve the changes.")
				.action("SAVE", _Controller).create();
		JButton bttnClose = UIBuilder.buildButton().text("Close").toolTip("Close the editor.")
				.action("CLOSE", _Controller).create();
		bttnPanel.add(bttnReset);
		bttnPanel.add(bttnOK);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// Invoke later because there will be no containing parent at
				// construction time.
				if (getContainingDialog() != null)
					bttnPanel.add(bttnClose);
			}
		});

		_Editor = new JScriptEditor(securityLevel);
		_Editor.setPreferredSize(new Dimension(550, 500));
		_Editor.setBorder(BorderFactory.createTitledBorder("Choose a script to edit."));
		_Editor.addActionListener(_Controller);

		JPanel scriptPanel = new JPanel();
		scriptPanel.setLayout(new BorderLayout());
		scriptPanel.add(scriptScroller, BorderLayout.LINE_START);
		scriptPanel.add(_Editor, BorderLayout.CENTER);
		scriptPanel.add(bttnPanel, BorderLayout.PAGE_END);

		JPanel propertiesPanel = new JPanel();
		propertiesPanel.setLayout(new BorderLayout());
		propertiesPanel.add(new JLabel("To be determined..."), BorderLayout.CENTER);

		JCodeREPL repl = new JCodeREPL(entity.getSandbox());
		repl.addActionListener(_Controller);


		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Command Line", null, repl, "Instantaneous script runner.");
		tabPane.addTab("Scripts", null, scriptPanel, "Scripts relating to this entity.");
		tabPane.addTab("Properties", propertiesPanel);


		this.setLayout(new HorizontalLayout());
		this.add(tabPane, BorderLayout.LINE_START);


	}


	/** Resets all characteristics of this entity to the original state. */
	public void reset() {
		_ScriptList.clear();
		for (UserScript u : _Entity.getScripts())
			_ScriptList.addElement(u.copy());
	}


	/**
	 * Saves all characteristics to the Entity as they are presented in this
	 * GUI.
	 */
	public void save() {
		_Entity.getScripts().clear();
		_Editor.saveScript();
		for (int i = 0; i < _ScriptList.getSize(); i++) {
			UserScript script = _ScriptList.get(i);
			_Entity.getScripts().add(script);
		}
	}


	// ===================================================
	// ========= JEntityEditor help stuff ================
	// ===================================================


	private JFrame _CurrentHelpFrame = null;
	private Document _CurrentHelpDocument = null;


	// Shows the help frame, if there is not one showing already.
	private void showHelp() {
		if (_CurrentHelpFrame != null)
			return;
		_CurrentHelpFrame = new JFrame();
		_CurrentHelpFrame.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
		JEditorPane textPane = new JEditorPane();
		SecurityLevel securityLevel = JEntityEditor.this._SecurityLevel;
		textPane.setEditable(securityLevel.level >= SecurityLevel.AUTHOR.level);
		textPane.setText(_Entity.help);
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
						_Entity.help = _CurrentHelpDocument.getText(0, _CurrentHelpDocument.getLength());
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


	/** The controller for this editor. */
	private class Controller implements ActionListener, ListSelectionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "RESET":
				reset();
				return;
			case "SAVE":
				save();
				return;
			case "CLOSE":
				JDialog dialog = getContainingDialog();
				if (dialog != null)
					dialog.dispose();
				return;
			case "HELP":
				showHelp();
				return;
			default:
				System.out.println("JEntityEditor has not implemented command " + e.getActionCommand());
				return;
			}
		}


		/** Called when the script selection list changes its selection. */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int idx = e.getFirstIndex();
			if (idx != e.getLastIndex())
				throw new IllegalStateException("Only one item can be selected at a time.");

			@SuppressWarnings("unchecked")
			JList<UserScript> list = (JList<UserScript>) e.getSource();

			UserScript script = list.getSelectedValue();

			_Editor.saveScript();
			_Editor.setScript(script);
			_Editor.setBorder(BorderFactory.createTitledBorder(script.name));
		}
	}


}
