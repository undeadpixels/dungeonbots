package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.math.WindowListenerAdapter;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;
import com.undead_pixels.dungeon_bots.ui.code_edit.JScriptEditor;
import com.undead_pixels.dungeon_bots.ui.screens.GameplayScreen;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

public final class JEntityEditor extends JPanel {

	private static final HashMap<Entity, JDialog> _OpenEditors = new HashMap<Entity, JDialog>();

	private Entity _Entity;
	private JScriptEditor _Editor;
	private Controller _Controller;

	public static JEntityEditor create(java.awt.Window owner, Entity entity, SecurityLevel securityLevel,
			String title) {

		if (_OpenEditors.containsKey(entity)) {
			System.err.println("An editor is already open for this entity:  " + entity.toString());
			JDialog dialog = _OpenEditors.get(entity);
			dialog.requestFocus();
			return null;
		}

		JEntityEditor jpe = new JEntityEditor(entity, securityLevel);
		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		dialog.add(jpe);
		dialog.pack();
		dialog.addWindowListener(new WindowListenerAdapter() {
			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING)
					return;
				_OpenEditors.remove(entity);
			}
		});
		dialog.setVisible(true);
		_OpenEditors.put(entity, dialog);
		return jpe;
	}

	private JEntityEditor(Entity entity, SecurityLevel securityLevel) {
		super(new BorderLayout());

		_Entity = entity;
		_Controller = new Controller();

		// this.setPreferredSize(new Dimension(800, 600));

		DefaultListModel<UserScript> listModel = new DefaultListModel<UserScript>();
		for (UserScript u : entity.userScripts)
			listModel.addElement(u);
		JList<UserScript> scriptList = new JList<UserScript>(listModel);
		scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptList.setLayoutOrientation(JList.VERTICAL);
		scriptList.setVisibleRowCount(-1);
		scriptList.addListSelectionListener(_Controller);
		JScrollPane scriptScroller = new JScrollPane(scriptList);
		scriptScroller.setPreferredSize(new Dimension(200, this.getSize().height - 100));
		scriptScroller.setBorder(BorderFactory.createTitledBorder("Scripts for this Entity"));

		JPanel bttnPanel = new JPanel();
		bttnPanel.setLayout(new GridLayout(1, 3, 10, 10));
		JButton bttnReset = UIBuilder.makeButton("", "Reset the entity characteristics.", "RESET", _Controller);
		JButton bttnOK = UIBuilder.makeButton("", "Approve changes.", "APPROVE", _Controller);
		JButton bttnCancel = UIBuilder.makeButton("", "Cancel without saving changes.", "CANCEL", _Controller);
		bttnPanel.add(bttnReset);
		bttnPanel.add(bttnOK);
		bttnPanel.add(bttnCancel);

		_Editor = new JScriptEditor(securityLevel);
		_Editor.setPreferredSize(new Dimension(550, 500));
		_Editor.setBorder(BorderFactory.createTitledBorder("Choose a script to edit."));

		JPanel scriptPanel = new JPanel();
		scriptPanel.setLayout(new BorderLayout());
		scriptPanel.add(scriptScroller, BorderLayout.LINE_START);
		scriptPanel.add(_Editor, BorderLayout.CENTER);
		scriptPanel.add(bttnPanel, BorderLayout.PAGE_END);

		JCodeREPL repl = new JCodeREPL(entity.getSandbox());

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("REPL", null, repl, "Instantaneous script runner.");
		tabPane.addTab("Scripts", null, scriptPanel, "Scripts relating to this entity.");

		this.add(tabPane, BorderLayout.LINE_START);

	}

	private class Controller implements ActionListener, ListSelectionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int idx = e.getFirstIndex();
			if (idx != e.getLastIndex())
				throw new IllegalStateException("Only one item can be selected at a time.");

			@SuppressWarnings("unchecked")
			JList<UserScript> list = (JList<UserScript>) e.getSource();

			UserScript script = list.getSelectedValue();

			_Editor.setScript(script);
			_Editor.setBorder(BorderFactory.createTitledBorder(script.name));
		}
	}

}
