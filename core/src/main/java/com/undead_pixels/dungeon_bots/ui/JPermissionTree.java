package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;

@SuppressWarnings("serial")
public class JPermissionTree extends JTree {

	private static final Dimension LABEL_DIMENSION = new Dimension(150, 20);
	private static final Dimension DOT_DIMENSION = new Dimension(18, 18);
	private DotIcon[] icons;

	private final ArrayList<Permission> permissions;
	private Dialog dialog = null;
	private SecurityLevel[] availableLevels = SecurityLevel.values();
	private boolean changed = false;


	JPermissionTree() {
		this.permissions = new ArrayList<Permission>();
		this.setCellRenderer(new TreeRenderer());
		this.setCellEditor(new TreeEditor((DefaultTreeCellRenderer) this.getCellRenderer()));
		this.setEditable(true);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setRootVisible(false);
		this.availableLevels = new SecurityLevel[] { SecurityLevel.AUTHOR, SecurityLevel.ENTITY, SecurityLevel.TEAM,
				SecurityLevel.NONE };
		setColors(new Color[] { Color.red, Color.CYAN, Color.YELLOW, Color.GREEN, Color.BLUE, Color.magenta });
		updateGUI();
	}


	public void setColors(Color[] newColors) {
		if (newColors == null || newColors.length < availableLevels.length)
			throw new RuntimeException("There must be a color for every security level.");
		DotIcon[] newIcons = new DotIcon[newColors.length];
		for (int i = 0; i < newColors.length; i++) {
			Color c = newColors[i];
			newIcons[i] = new DotIcon(c, DOT_DIMENSION.width, DOT_DIMENSION.height, 3);
		}
		icons = newIcons;
		repaint();
	}


	/**Determines what security levels are available.*/
	public void setSecurityLevels(SecurityLevel[] levels) {
		if (levels == null)
			throw new RuntimeException("Levels cannot be null.");
		if (levels.length > icons.length)
			throw new RuntimeException("There must be at least a color for every security level.");
		this.availableLevels = levels;
		updateGUI();
	}


	/**Sets the current contents of the white list.*/
	public void setItems(Whitelist whitelist) {
		permissions.clear();
		for (Entry<String, SecurityLevel> entry : whitelist) {
			String id = entry.getKey();
			Permission p = new Permission(id, entry.getValue(), whitelist.getInfo(entry.getKey()));
			permissions.add(p);
		}
		this.updateGUI();
	}


	/**Adds a line for the specific permission.*/
	public void addPermission(String name, SecurityLevel startingLevel, String helpInfo) {
		Permission p = new Permission(name, startingLevel, helpInfo);
		permissions.add(p);
		this.updateGUI();
	}


	/**Returns the current permission map.*/
	public HashMap<String, SecurityLevel> getPermissionMap() {
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
				currentBranch = new Permission(p.type, null, "Change all security levels for type " + p.type);
				root.insert(currentBranch, root.getChildCount());
				currentBranch.insert(p, 0);
			} else {
				// Add to existing branch.
				currentBranch.insert(p, currentBranch.getChildCount());
			}
		}
		this.setModel(model);
	}


	/**The Editor makes the lines of the rendered tree responsive to user input.*/
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


	/**Controls how the lines of the tree appear.*/
	private final class TreeRenderer extends DefaultTreeCellRenderer {


		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object obj, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			if (obj instanceof Permission) {
				Permission p = (Permission) obj;

				JPanel pnl = new JPanel(new HorizontalLayout());
				pnl.setOpaque(false);
				JLabel lbl;

				if (leaf) {
					lbl = new JLabel(p.label, SwingConstants.RIGHT);

				} else {
					lbl = new JLabel(p.label, SwingConstants.LEFT);
				}
				lbl.setPreferredSize(LABEL_DIMENSION);
				pnl.add(lbl);


				ButtonGroup group = new ButtonGroup();
				JRadioButton[] bttns = new JRadioButton[availableLevels.length];
				ActionListener listener = new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						int idx = 0;
						for (; idx < bttns.length; idx++)
							if (e.getSource().equals(bttns[idx]))
								break;
						if (availableLevels[idx].equals(p.level))
							return;
						p.level = availableLevels[idx];
						changed = true;


						if (!p.isLeaf()) {
							if (p.level != null) {
								for (idx = 0; idx < p.getChildCount(); idx++) {
									((Permission) p.getChildAt(idx)).level = p.level;
								}
							}
						} else if (p.getParent() instanceof Permission) {
							Permission parent = (Permission) p.getParent();
							parent.level = null;
						}
						JPermissionTree.this.repaint();
					}

				};
				for (int i = 0; i < availableLevels.length; i++) {
					JRadioButton bttn = new JRadioButton();
					Icon icon = icons[i];
					bttn.setSelectedIcon(icon);
					bttn.addActionListener(listener);
					bttn.setToolTipText(availableLevels[i].toString().toLowerCase());
					if (availableLevels[i].equals(p.level))
						bttn.setSelected(true);
					else
						bttn.setSelected(false);
					group.add(bttn);
					bttns[i] = bttn;
					pnl.add(bttn);
				}

				return pnl;

			} else
				return new JLabel(obj.toString());
		}

	};


	// ============================================
	// ==== JPermissionTree DIALOG STUFF ==========
	// ============================================
	private static JPermissionTree oneDialogAllowed = null;


	/**Returns a JPermissionEditor packed into a JDialog container.  If the user approves 
	 * changes in the dialog, the given onCommit function is called.*/
	public static JPermissionTree createDialog(java.awt.Window owner, String title,
			BiConsumer<HashMap<String, SecurityLevel>, HashMap<String, String>> onCommit) {

		if (oneDialogAllowed != null) {
			oneDialogAllowed.requestFocus();
			return oneDialogAllowed;
		}

		// Create the dialog.
		JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
		dialog.setLayout(new BorderLayout());

		// Create the editor.
		JPermissionTree jpe = new JPermissionTree();
		oneDialogAllowed = jpe;
		jpe.dialog = dialog;
		JScrollPane scroller = new JScrollPane(jpe);
		scroller.setPreferredSize(new Dimension(400, 400));
		scroller.setBorder(new EmptyBorder(5, 5, 5, 5));
		dialog.add(scroller, BorderLayout.CENTER);

		// Create the info panel.
		JTextPane infoPanel = new JTextPane();
		infoPanel.setEditable(false);
		infoPanel.setPreferredSize(new Dimension(300, 400));
		infoPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoPanel.setVisible(false);
		dialog.add(infoPanel, BorderLayout.LINE_END);

		// A listener will change the contents and visibility of the info panel.
		jpe.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Permission selection = jpe.getSelection();
				String info = (selection == null) ? "" : selection.info;
				if (info == null || info.equals(""))
					infoPanel.setVisible(false);
				else {
					infoPanel.setText(info);
					infoPanel.setVisible(true);
				}
				dialog.pack();
			}

		});


		ActionListener dialogController = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				switch (arg0.getActionCommand()) {
				case "COMMIT":
					dialog.dispose();
					onCommit.accept(jpe.getPermissionMap(), jpe.getInfoMap());
					break;
				case "CANCEL":
					if (jpe.changed) {
						int confirm = JOptionPane.showConfirmDialog(jpe, "Discard all changes?", "Confirm",
								JOptionPane.YES_NO_OPTION);
						if (confirm != JOptionPane.YES_OPTION)
							break;
					}
					jpe.permissions.clear();
					dialog.dispose();
					break;
				case "HELP":
				default:
					throw new RuntimeException("Not implemented command " + arg0.getActionCommand());
				}


			}

		};
		dialog.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				oneDialogAllowed = null;
				jpe.dialog = null;
			}
		});

		// Create the approval buttons.
		JPanel pnlButtons = new JPanel(new HorizontalLayout());
		pnlButtons.add(UIBuilder.buildButton().image("icons/ok.png").toolTip("Approve changes and close the dialog.")
				.action("COMMIT", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/close.png").toolTip("Cancel changes and close the dialog.")
				.action("CANCEL", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/question.png").toolTip("Help on the world sizer.")
				.action("HELP", dialogController).create());
		dialog.add(pnlButtons, BorderLayout.PAGE_END);

		// Pack and display
		dialog.pack();
		return jpe;
	}


	/**Sets this tree visibility.  If the tree is associated with a dialog, sets the dialog visibility.*/
	@Override
	public void setVisible(boolean value) {
		if (dialog != null) {
			dialog.setVisible(value);
			super.setVisible(value);
		}

		else
			super.setVisible(value);
	}


	/**Returns the permission currently selected.*/
	private Permission getSelection() {
		TreePath path = getSelectionPath();
		if (path == null)
			return null;
		return (Permission) path.getLastPathComponent();
	}


	private static class Permission extends DefaultMutableTreeNode {

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


		@Override
		public String toString() {
			String str = (type == null || type.equals("")) ? "" : type + ":";
			str += label + "   " + (level != null ? level.toString() : "<null>");
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
