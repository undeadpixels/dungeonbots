package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeEditor;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

public class JEntityEditor extends JPanel {

	private Entity _Entity;
	private JCodeEditor _Editor;

	private ActionListener _Controller = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub

		}

	};

	private ListSelectionListener _SelectionListener = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int idx = e.getFirstIndex();
			if (idx != e.getLastIndex())
				throw new IllegalStateException("Only one item can be selected at a time.");

			@SuppressWarnings("unchecked")
			JList<UserScript> list = (JList<UserScript>) e.getSource();

			UserScript script = list.getSelectedValue();

			_Editor.setCode(script.code);
			_Editor.setBorder(BorderFactory.createTitledBorder(script.name));
		}

	};

	/*
	 * private static class UserScriptRenderer extends JLabel implements
	 * ListCellRenderer<UserScript>{
	 * 
	 * @Override public Component getListCellRendererComponent(JList<? extends
	 * UserScript> list, UserScript item, int index, boolean isSelected, boolean
	 * cellHasFocus) { this.setText(item.name); if (isSelected)
	 * setBackground(Color.blue); return this; }
	 * 
	 * }
	 */

	public JEntityEditor(Entity entity) {
		super(new BorderLayout());
		_Entity = entity;

		this.setPreferredSize(new Dimension(800, 600));

		DefaultListModel<UserScript> listModel = new DefaultListModel<UserScript>();
		for (UserScript u : entity.userScripts)
			listModel.addElement(u);
		JList<UserScript> scriptList = new JList<UserScript>(listModel);
		scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptList.setLayoutOrientation(JList.VERTICAL);
		scriptList.setVisibleRowCount(-1);
		scriptList.addListSelectionListener(_SelectionListener);
		JScrollPane scriptScroller = new JScrollPane(scriptList);
		scriptScroller.setPreferredSize(new Dimension(200, this.getSize().height - 100));
		scriptScroller.setBorder(BorderFactory.createTitledBorder("Scripts for this Entity"));

		JPanel bttnPanel = new JPanel();
		bttnPanel.setLayout(new GridLayout(1, 3, 10, 10));
		JButton bttnReset = UIBuilder.makeButton("", "Reset the entity characteristics.", "Reset", "RESET",
				_Controller);
		JButton bttnOK = UIBuilder.makeButton("", "Approve changes.", "OK", "APPROVE", _Controller);
		JButton bttnCancel = UIBuilder.makeButton("", "Cancel without saving changes.", "Cancel", "CANCEL",
				_Controller);
		bttnPanel.add(bttnReset);
		bttnPanel.add(bttnOK);
		bttnPanel.add(bttnCancel);

		_Editor = new JCodeEditor();
		_Editor.setPreferredSize(new Dimension(550,500));
		_Editor.setBorder(BorderFactory.createTitledBorder("Choose a script to edit."));

		JPanel scriptPanel = new JPanel();
		scriptPanel.setLayout(new BorderLayout());
		scriptPanel.add(scriptScroller, BorderLayout.LINE_START);
		scriptPanel.add(_Editor, BorderLayout.CENTER);
		scriptPanel.add(bttnPanel, BorderLayout.PAGE_END);

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Scripts", null, scriptPanel, "Scripts relating to this entity.");

		this.add(tabPane, BorderLayout.LINE_START);

		
	}

}
