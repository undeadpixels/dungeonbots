package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.code_edit.JScriptEditor;

@SuppressWarnings("serial")
public class JScriptCollectionEditor extends JPanel {


	private final JScriptEditor editor;
	private JList<UserScript> scriptList;
	private final SecurityLevel security;
	private final UserScriptCollection scripts;


	public JScriptCollectionEditor(LuaSandbox sandbox, UserScriptCollection scripts, SecurityLevel security) {
		editor = new JScriptEditor(security);
		this.security = security;
		// Create the editor.
		editor.setBorder(BorderFactory.createTitledBorder("Choose a script to edit."));
		editor.setEnabled(false);
		editor.setEditable(true);

		// Create the list.
		if(scripts.isEmpty()) {
			scripts.add(new UserScript("init", "--Write your new script here."));
		}
		this.scripts = scripts;
		UserScript[] scriptsSorted = scripts.toArray();
		scriptList = new JList<UserScript>();
		DefaultListModel<UserScript> model = new DefaultListModel<UserScript>();
		for (UserScript u : scriptsSorted)
			model.addElement(u);
		scriptList.setModel(model);
		scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptList.setLayoutOrientation(JList.VERTICAL);
		scriptList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				editor.saveScript();
				editor.setScript(scriptList.getSelectedValue());
			}
		});
		scriptList.setCellRenderer(new ListCellRenderer<UserScript>() {

			@Override
			public Component getListCellRendererComponent(JList<? extends UserScript> list, UserScript script,
					int index, boolean isSelected, boolean hasFocus) {
				String name = (script.name == null || script.name.equals("")) ? "Script " + index : script.name;
				JLabel lbl = new JLabel(name);
				if (isSelected)
					lbl.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
				lbl.setPreferredSize(new Dimension(150, 20));
				return lbl;
			}
		});
		if (scriptList.getModel().getSize() > 0)
			scriptList.setSelectedIndex(0);
		else
			editor.setScript(null);

		JScrollPane scriptScroller = new JScrollPane(scriptList);
		
		Box scriptListBox = new Box(BoxLayout.Y_AXIS);
		scriptListBox.setBorder(BorderFactory.createTitledBorder("Editable scripts"));

		CodeInsertions codeInsertions = new CodeInsertions(sandbox);
		JScrollPane insertionScroller = codeInsertions.makeScrollerAndSetup(editor.getEditor());

		// Make buttons for add/remove of scripts.
		Box bttnPanel = new Box(BoxLayout.X_AXIS);
		bttnPanel.add(Box.createGlue());
		bttnPanel.add(UIBuilder.buildButton().image("icons/add.png").toolTip("Add a script to the list")
				.action("ADD_SCRIPT", controller).create());
		bttnPanel.add(UIBuilder.buildButton().image("icons/erase.png").toolTip("Remove a script from the list")
				.action("REMOVE_SCRIPT", controller).create());
		Box leftBox = new Box(BoxLayout.Y_AXIS);
		bttnPanel.add(Box.createGlue());
		bttnPanel.setMaximumSize(bttnPanel.getPreferredSize());

		scriptListBox.add(bttnPanel);
		scriptListBox.add(scriptScroller);
		scriptList.setPreferredSize(new Dimension(scriptList.getPreferredSize().width, 100));
		insertionScroller.setPreferredSize(new Dimension(insertionScroller.getPreferredSize().width, 250));
		leftBox.add(scriptListBox);
		leftBox.add(insertionScroller);
		

		this.setLayout(new BorderLayout());
		this.add(leftBox, BorderLayout.LINE_START);
		this.add(editor, BorderLayout.CENTER);
	}


	public UserScript[] getScripts() {

		UserScript[] ret = new UserScript[scriptList.getModel().getSize()];
		for (int i = 0; i < scriptList.getModel().getSize(); i++)
			ret[i] = scriptList.getModel().getElementAt(i);
		return ret;
	}


	public boolean save() {
		return editor.saveScript();
	}


	private final ActionListener controller = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			int idx = scriptList.getSelectedIndex();
			// Object m = scriptList.getModel();
			DefaultListModel<UserScript> model = (DefaultListModel<UserScript>) scriptList.getModel();
			switch (arg0.getActionCommand()) {
			case "ADD_SCRIPT":
				String scriptName = JOptionPane.showInputDialog(JScriptCollectionEditor.this, "Enter script name:", "",
						JOptionPane.QUESTION_MESSAGE);
				if (scriptName == null || scriptName.equals("")) {
					JOptionPane.showMessageDialog(JScriptCollectionEditor.this, "Invalid script name.");
					return;
				}
				for (int i = 0; i < model.getSize(); i++) {
					if (model.getElementAt(i).name.equals(scriptName)) {
						JOptionPane.showMessageDialog(JScriptCollectionEditor.this,
								"Duplicate script names not allowed.");
						return;
					}
				}
				if (idx < 0)
					idx = scriptList.getModel().getSize();
				UserScript newScript = new UserScript(scriptName, "--Write your new script here.", security);
				model.insertElementAt(newScript, idx);
				scripts.add(newScript);
				scriptList.setSelectedValue(newScript, true);
				return;
			case "REMOVE_SCRIPT":
				if (idx >= 0) {
					UserScript toRemove = model.getElementAt(idx);
					model.remove(idx);
					scriptList.setSelectedValue(null, true);
					scripts.remove(toRemove.name);
				}
				return;
			}

		}
	};
}
