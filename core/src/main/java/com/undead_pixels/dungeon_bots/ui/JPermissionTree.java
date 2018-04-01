package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;

@SuppressWarnings("serial")
public class JPermissionTree extends JTree {

	private static final Dimension LABEL_DIMENSION = new Dimension(150, 30);
	private static final Dimension COLOR_DIMENSION = new Dimension(20, 20);
	private static final Border UNSELECTED_BORDER = new EmptyBorder(5, 5, 5, 5);
	private static final Color[] colors = new Color[] { Color.red, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE,
			Color.magenta };

	private final ArrayList<Permission> permissions;
	private Dialog dialog;
	private final Undoable.Listener undoableListener;
	private final ArrayList<ListSelectionListener> selectionListeners;
	private SecurityLevel[] availableLevels = SecurityLevel.values();


	private JPermissionTree(JDialog dialog, Undoable.Listener undoableListener) {
		this.undoableListener = undoableListener;
		this.dialog = dialog;
		this.permissions = new ArrayList<Permission>();
		this.selectionListeners = new ArrayList<ListSelectionListener>();
		this.setCellRenderer(new TreeRenderer());
		this.setCellEditor(new TreeEditor((DefaultTreeCellRenderer) this.getCellRenderer()));
		this.setEditable(true);

		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setRootVisible(false);


		updateGUI();
	}


	/**Completely redraws the entire GUI.*/
	private void updateGUI() {

		// this.removeAll();
		permissions.sort(Permission.comparator);
		Permission currentBranch = null;

		MutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultTreeModel model = new DefaultTreeModel(root);

		for (Permission p : permissions) {
			if (p.type == null || p.type.equals("")) {
				// Cannot make a branch
				root.insert(p, root.getChildCount());
				currentBranch = null;
			} else if (currentBranch == null || !p.type.equals(currentBranch.label)) {
				// Switch to a new branch.
				currentBranch = new Permission(p.type, SecurityLevel.NONE,
						"Change all security levels for type " + p.type);
				root.insert(currentBranch, root.getChildCount());
				currentBranch.insert(p, 0);
			} else {
				// Add to existing branch.
				currentBranch.insert(p, currentBranch.getChildCount());
			}
		}
		this.setModel(model);
	}


	/**Determines what security levels are available.*/
	public void setSecurityLevels(SecurityLevel[] levels) {
		this.availableLevels = levels;
		updateGUI();
	}


	public void addListSelectionListener(ListSelectionListener l) {
		selectionListeners.add(l);
	}


	public void setItems(Whitelist whitelist) {

		permissions.clear();

		for (Entry<String, SecurityLevel> entry : whitelist) {
			String id = entry.getKey();
			Permission p = new Permission(id, entry.getValue(), whitelist.getInfo(entry.getKey()));
			permissions.add(p);
		}


		this.updateGUI();
	}


	private final class TreeEditor extends DefaultTreeCellEditor {

		public TreeEditor(DefaultTreeCellRenderer renderer) {
			super(JPermissionTree.this, renderer);
		}


		@Override
		public boolean isCellEditable(EventObject arg0) {
			return true;
		}


		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row) {
			return renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		}


	};


	private final class TreeRenderer extends DefaultTreeCellRenderer {

		class RadioListener implements ActionListener {

			private final JButton[] buttons;
			private final Permission permission;


			public RadioListener(Permission permission, JButton[] buttons) {
				this.buttons = buttons;
				this.permission = permission;
			}


			@Override
			public void actionPerformed(ActionEvent e) {
				// Which button was clicked?
				int idx = 0;
				for (; idx < buttons.length; idx++) {
					if (buttons[idx].equals(e.getSource()))
						break;
				}

				// Set the permission's level accordingly.
				permission.level = availableLevels[idx];

				// Update all the buttons' appearances.
				updateButtonsAppearance(permission.level, buttons);
			}
		}


		private void updateButtonsAppearance(SecurityLevel level, JButton[] buttons) {
			for (int i = 0; i < buttons.length; i++) {
				if (level.equals(availableLevels[i])) {
					buttons[i].setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, colors[i]));
					buttons[i].setBackground(Color.white);
				} else {
					buttons[i].setBorder(UNSELECTED_BORDER);
					buttons[i].setBackground(colors[i]);
				}
			}
		}


		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object obj, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			if (obj instanceof Permission) {
				Permission p = (Permission) obj;

				// If it's a type branch, just return a label.
				if (p.type == null || p.type.equals(""))
					return new JLabel(p.label);

				// Otherwise, return a label with security options.
				JPanel pnl = new JPanel(new HorizontalLayout());
				pnl.setOpaque(false);
				JLabel lbl = new JLabel(p.label, SwingConstants.RIGHT);
				lbl.setPreferredSize(LABEL_DIMENSION);
				pnl.add(lbl);
				JButton[] bttns = new JButton[availableLevels.length];
				RadioListener listener = new RadioListener(p, bttns);
				for (int i = 0; i < availableLevels.length; i++) {
					JButton bttn = new JButton();
					bttns[i] = bttn;
					bttn.setPreferredSize(COLOR_DIMENSION);
					SecurityLevel level = availableLevels[i];
					bttn.setToolTipText(level.toString());
					bttn.addActionListener(listener);
					pnl.add(bttn);
				}
				updateButtonsAppearance(p.level, bttns);
				return pnl;

			} else
				return new JLabel(obj.toString());
		}

	};


	/**Writes the current permissions to the given whitelist.*/
	public void writeTo(Whitelist whitelist) {
		HashMap<String, SecurityLevel> securityLevels = new HashMap<String, SecurityLevel>();
		HashMap<String, String> infos = new HashMap<String, String>();
		for (Permission p : permissions) {
			securityLevels.put(p.id, p.level);
			infos.put(p.id, p.info);
		}
		whitelist.setLevels(securityLevels, infos);
	}


	/**Returns the current permission map.*/
	public HashMap<String, SecurityLevel> getPermissionsMap() {
		HashMap<String, SecurityLevel> securityLevels = new HashMap<String, SecurityLevel>();
		for (Permission p : permissions) {
			securityLevels.put(p.id, p.level);
		}
		return securityLevels;
	}


	/**Returns the current info (help text) map.*/
	public HashMap<String, String> getInfoMap() {
		HashMap<String, String> result = new HashMap<>();
		for (Permission p : permissions) {
			result.put(p.id, p.info);
		}
		return result;
	}


	/**Returns a JPermissionEditor packed into a JDialog container.*/
	public static JPermissionTree createDialog(java.awt.Window owner, String title,
			Undoable.Listener undoableListener) {

		// Create the dialog.
		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		dialog.setLayout(new BorderLayout());

		// Create the editor.
		JPermissionTree jpe = new JPermissionTree(dialog, undoableListener);
		// jpe.setBackground(Color.green);
		JScrollPane scroller = new JScrollPane(jpe);
		scroller.setPreferredSize(new Dimension(400, 400));
		scroller.setBorder(new EmptyBorder(5, 5, 5, 5));
		dialog.add(scroller, BorderLayout.CENTER);

		// Create the info panel.
		JTextPane infoPanel = new JTextPane();
		infoPanel.setEditable(false);
		JXCollapsiblePane c = new JXCollapsiblePane();
		c.setMinimumSize(new Dimension(300, -1));
		c.add(infoPanel);
		c.setBorder(new EmptyBorder(5, 5, 5, 5));
		c.setCollapsed(true);
		jpe.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Permission selection = jpe.getSelection();
				String info = (selection == null) ? "" : selection.info;
				infoPanel.setText(info);
				c.setCollapsed(!(info != null && !info.equals("")));
				dialog.pack();
			}

		});


		dialog.add(infoPanel, BorderLayout.LINE_END);

		// Create the approval buttons.
		JPanel pnlButtons = new JPanel(new HorizontalLayout());
		pnlButtons.add(UIBuilder.buildButton().image("icons/ok.png").toolTip("Approve changes and close the dialog.")
				.action("COMMIT", jpe.dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/ok.png").toolTip("Cancel changes and close the dialog.")
				.action("CANCEL", jpe.dialogController).create());
		dialog.add(pnlButtons, BorderLayout.PAGE_END);


		// Add stuff that will manage the dialog and editor when the dialog is
		// closed.
		dialog.pack();
		dialog.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				jpe.dialog = null;
			}
		});

		return jpe;
	}


	private final ActionListener dialogController = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			switch (arg0.getActionCommand()){
			default:
				System.out.println("JPermissionTree.dialogController has not implemented command: " + arg0.getActionCommand());			
			}


		}

	};


	@Override
	public void setVisible(boolean value) {
		if (dialog != null) {
			dialog.setVisible(value);
			super.setVisible(value);
		}

		else
			super.setVisible(value);
	}


	Permission getSelection() {
		TreePath path = getSelectionPath();
		if (path == null)
			return null;
		return (Permission) path.getLastPathComponent();
	}


	@SuppressWarnings("serial")
	static class Permission extends DefaultMutableTreeNode {

		public final String id;
		public final String type;
		public final String label;
		public SecurityLevel level;
		public String info;


		public Permission(String id, SecurityLevel level, String info) {
			this.id = id;
			String l = id;
			String split[] = id.split("\\.");
			if (split.length > 0)
				l = split[split.length - 1];
			split = l.split(":");
			if (split.length > 1) {
				type = split[0];
				label = split[1];
			} else {
				type = "";
				label = l;
			}
			this.level = level;
			this.info = info;
		}


		@Override
		public String getUserObject() {
			return (String) super.getUserObject();
		}


		public void setUserObject(String string) {
			super.setUserObject(string);

		}


		@Override
		public String toString() {
			String str = (type == null || type.equals("")) ? "" : type + ":";
			str += label + "   " + level.toString();
			str += (info == null || info.equals("") ? "<no info>" : info.substring(0, Math.min(10, info.length())));
			return str;
		}


		public static final Comparator<Permission> comparator = new Comparator<Permission>() {

			@Override
			public int compare(Permission a, Permission b) {
				int cmp = a.type.compareTo(b.type);
				if (cmp == 0)
					cmp = a.label.compareTo(b.label);
				return cmp;
			}

		};


	}


}
