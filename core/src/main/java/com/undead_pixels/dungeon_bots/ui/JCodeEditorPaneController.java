/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import com.undead_pixels.dungeon_bots.ui.code_edit.JScriptEditor;

/**
 *
 */
public class JCodeEditorPaneController {
	
	private final GetLuaSandbox sandboxable;
	private final JScriptEditor editor;
	private final DefaultListModel<UserScript> scriptsModel;
	private final Controller controller;

	/**
	 * Saves all characteristics to the Entity as they are presented in this
	 * GUI.
	 */
	public void save() {
		sandboxable.getScripts().clear();
		editor.saveScript();
		for (int i = 0; i < scriptsModel.getSize(); i++) {
			UserScript script = scriptsModel.get(i);
			sandboxable.getScripts().add(script);
		}
	}
	
	public JCodeEditorPaneController(GetLuaSandbox entity, SecurityLevel securityLevel) {
		sandboxable = entity;
		editor = new JScriptEditor(securityLevel);
		scriptsModel = new DefaultListModel<UserScript>();
		controller = new Controller();
	}
	
	public JComponent create() {
		reset();

		editor.setPreferredSize(new Dimension(550, 500));
		editor.setBorder(BorderFactory.createTitledBorder("Choose a script to edit."));

		JList<UserScript> scriptList = new JList<UserScript>(this.scriptsModel);
		scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptList.setLayoutOrientation(JList.VERTICAL);
		scriptList.setVisibleRowCount(-1);
		scriptList.addListSelectionListener(controller);
		JScrollPane scriptScroller = new JScrollPane(scriptList);
		scriptScroller.setPreferredSize(new Dimension(200, 400));
		scriptScroller.setBorder(BorderFactory.createTitledBorder("Scripts for this Entity"));

		JPanel bttnPanel = new JPanel();
		bttnPanel.setLayout(new GridLayout(1, 3, 10, 10));
		JButton bttnReset = UIBuilder.buildButton().text("Reset").toolTip("Reset the entity characteristics.")
				.action("RESET", controller).create();
		JButton bttnOK = UIBuilder.buildButton().text("Save").toolTip("Approve the changes.")
				.action("SAVE", controller).create();
		JButton bttnClose = UIBuilder.buildButton().text("Close").toolTip("Close the editor.")
				.action("CLOSE", controller).create();
		bttnPanel.add(bttnReset);
		bttnPanel.add(bttnOK);
		bttnPanel.add(bttnClose);

		JPanel scriptPanel = new JPanel();
		scriptPanel.setLayout(new BorderLayout());
		scriptPanel.add(scriptScroller, BorderLayout.LINE_START);
		scriptPanel.add(editor, BorderLayout.CENTER);
		scriptPanel.add(bttnPanel, BorderLayout.PAGE_END);
		
		return scriptPanel;
	}

	/** Resets all characteristics of this entity to the original state. */
	public void reset() {
		scriptsModel.clear();
		for (UserScript u : sandboxable.getScripts()) {
			scriptsModel.addElement(u.copy());
		}
	}

	/** The controller for this editor. */
	private class Controller implements ActionListener, ListSelectionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "RESET":
				reset();
				break;
			case "SAVE":
				save();
				break;
			case "CLOSE":
				Object o  = e.getSource();
				if(o instanceof JComponent) {
					JDialog dialog = getContainingDialog((JComponent) o);
					if (dialog != null)
						dialog.dispose();
				}
			}

		}

		/**
		 * Used to find the containing dialog parent, if there is one. A
		 * JEntityEditor GUI that exists within a JDialog behaves differently from
		 * one who doesn't.
		 * @param c 
		 */
		private JDialog getContainingDialog(JComponent c) {
			Container parent = c.getParent();
			while (parent != null) {
				if (parent instanceof JDialog)
					return (JDialog) parent;
				parent = parent.getParent();
			}
			return null;
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

			editor.saveScript();
			editor.setScript(script);
			editor.setBorder(BorderFactory.createTitledBorder(script.name));
		}
	}
}
