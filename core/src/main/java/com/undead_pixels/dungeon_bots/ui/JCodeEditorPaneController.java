/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
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
	private final Controller controller;
	private JList<UserScript> scriptList;
	private final EntityState originalState;
	private final EntityState currentState;


	/**
	 * Saves all characteristics to the Entity as they are presented in this
	 * editor.
	 */
	public void save() {
		editor.saveScript();
		sandboxable.getScripts().clear();
		for (int i = 0; i < currentState.scripts.getSize(); i++) {
			UserScript script = currentState.scripts.get(i);
			sandboxable.getScripts().add(script);
		}
	}


	public JCodeEditorPaneController(Entity entity, SecurityLevel securityLevel) {
		sandboxable = entity;
		editor = new JScriptEditor(securityLevel);
		controller = new Controller();
		originalState = EntityState.read(entity);
		currentState = EntityState.read(entity);
	}


	/**Creates the GUI.*/
	public JComponent create() {
		reset();

		editor.setPreferredSize(new Dimension(550, 500));
		editor.setBorder(BorderFactory.createTitledBorder("Choose a script to edit."));
		editor.addActionListener(controller);
		editor.setEnabled(false);

		// Create the list.
		scriptList = new JList<UserScript>(this.currentState.scripts);
		scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptList.setLayoutOrientation(JList.VERTICAL);
		scriptList.setVisibleRowCount(-1);
		scriptList.addListSelectionListener(controller);
		if (!this.currentState.scripts.isEmpty())
			scriptList.setSelectedIndex(0);

		// Put the list in a scroll pane.
		JScrollPane scriptScroller = new JScrollPane(scriptList);
		scriptScroller.setPreferredSize(new Dimension(200, 400));
		scriptScroller.setBorder(BorderFactory.createTitledBorder("Scripts for this Entity"));

		// Make buttons for add/remove of scripts.
		JPanel addRemovePanel = new JPanel(new FlowLayout());
		addRemovePanel.add(UIBuilder.buildButton().text("Add").toolTip("Add a script to the list.")
				.action("ADD_SCRIPT", controller).create());
		addRemovePanel.add(UIBuilder.buildButton().text("Remove").toolTip("Remove a script from the list.")
				.action("REMOVE_SCRIPT", controller).create());

		// Put the bottom buttons
		JPanel bottomBttnPanel = new JPanel();
		bottomBttnPanel.setLayout(new GridLayout(1, 3, 10, 10));
		bottomBttnPanel.add(UIBuilder.buildButton().text("Reset").toolTip("Reset the entity characteristics.")
				.action("RESET", controller).create());
		bottomBttnPanel.add(UIBuilder.buildButton().text("Save").toolTip("Approve the changes.")
				.action("SAVE", controller).create());
		bottomBttnPanel.add(UIBuilder.buildButton().text("Close").toolTip("Close the editor.")
				.action("CLOSE", controller).create());

		// Stick the add/remove button with the scroll pane
		Box leftBox = new Box(BoxLayout.Y_AXIS);
		leftBox.add(scriptScroller);
		leftBox.add(addRemovePanel);

		// Do the overall layout
		JPanel scriptPanel = new JPanel();
		scriptPanel.setLayout(new BorderLayout());
		scriptPanel.add(leftBox, BorderLayout.LINE_START);
		scriptPanel.add(editor, BorderLayout.CENTER);
		scriptPanel.add(bottomBttnPanel, BorderLayout.PAGE_END);

		return scriptPanel;
	}


	/** Resets all current state to that of the original state. */
	public void reset() {
		currentState.help = originalState.help;
		currentState.scripts.clear();
		for (int i = 0; i < originalState.scripts.size(); i++) {
			currentState.scripts.addElement(originalState.scripts.getElementAt(i).copy());
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
				Object o = e.getSource();
				if (o instanceof JComponent) {
					JDialog dialog = getContainingDialog((JComponent) o);
					if (dialog != null)
						dialog.dispose();
				}
				break;
			case "ADD_SCRIPT":
				String name = JOptionPane.showInputDialog("Name your new script:", "");
				currentState.scripts.addElement(new UserScript(name, "--Write your new script here."));
				scriptList.setSelectedIndex(currentState.scripts.size() - 1);
				break;
			case "REMOVE_SCRIPT":
			default:
				System.out.println("JCodeEditorPaneController has not implemented command " + e.getActionCommand());
				return;
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

			// Sanity check - only one item at a time
			assert (e.getFirstIndex() == e.getLastIndex());
			if (e.getFirstIndex() < 0) {
				editor.setEnabled(false);
			}

			@SuppressWarnings("unchecked")
			JList<UserScript> list = (JList<UserScript>) e.getSource();

			UserScript script = list.getSelectedValue();

			editor.setEnabled(true);
			editor.saveScript();
			editor.setScript(script);
			editor.setBorder(BorderFactory.createTitledBorder(script.name));
		}
	}


	/**A class for storing an editable snapshot of an Entity.*/
	private static class EntityState {

		/***/
		public String help;
		public DefaultListModel<UserScript> scripts;


		private EntityState(Entity e, String help, UserScript[] s) {
			this.help = new String(help);
			this.scripts = new DefaultListModel<UserScript>();
			for (UserScript u : s)
				this.scripts.addElement(u);
		}


		public static EntityState read(Entity e) {
			return new EntityState(e, e.help, e.getScripts().toArray());
		}


		public void write(Entity e) {
			UserScript[] asArray = new UserScript[scripts.size()];
			for (int i = 0; i < asArray.length; i++)
				asArray[i] = scripts.getElementAt(i);
			e.setScripts(asArray);
			e.help = help;
		}

	}

}
