package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.undo.UndoStack;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;

/**
 * The screen where a user selects what level/pack they want to play
 */
public class LevelPackScreen extends Screen {

	public static final int THUMBNAIL_WIDTH = 75;
	public static final int THUMBNAIL_HEIGHT = 50;
	private static final long serialVersionUID = 1L;
	private JTree _Tree = null;
	private JPanel _InfoPnl = null;

	private JButton _BttnPlayLevel;
	private JButton _BttnEditLevel;
	private JButton _BttnAddWorld;
	private JButton _BttnRemoveWorld;
	private JButton _BttnWorldUp;
	private JButton _BttnWorldDown;

	// TODO: implement undo/redo
	private final UndoStack _UndoStack = new UndoStack();


	// ===============================================================
	// ========== LevelPackScreen CONSTRUCTORS ======================
	// ===============================================================


	private LevelPackScreen() {
	}


	/**Returns a LevelPackScreen using all the packs that exist in the given directory.*/
	public static LevelPackScreen fromDirectory(String directory) {
		File[] files = getPackFiles(directory);
		ArrayList<String> jsons = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			String json = Serializer.readStringFromFile(files[i].getPath());
			if (json == null)
				System.err.println("Could not read file: " + files[i].getPath());
			else
				jsons.add(json);
		}
		LevelPackScreen lps = fromJSONs(jsons.toArray(new String[jsons.size()]));
		return lps;
	}


	/**Returns a LevelPackScreen whose packs are built (partially) from the given JSON strings.*/
	public static LevelPackScreen fromJSONs(String[] jsons) {

		// Figure out which packs come from good JSONs.
		ArrayList<LevelPack> goodPacks = new ArrayList<LevelPack>();
		ArrayList<String> goodJsons = new ArrayList<String>();
		for (int i = 0; i < jsons.length; i++) {
			LevelPack lp = LevelPack.fromJsonPartial(jsons[i]);
			if (lp == null) {
				System.err.println("Unrecognized LevelPack format for partial read.");
				continue;
			}
			goodPacks.add(lp);
			goodJsons.add(jsons[i]);
		}

		// Build the screens.
		PackInfo[] pInfos = new PackInfo[goodPacks.size()];
		for (int i = 0; i < goodPacks.size(); i++)
			pInfos[i] = PackInfo.withJSON(goodPacks.get(i), goodJsons.get(i));
		LevelPackScreen lps = new LevelPackScreen();
		lps.setup();
		lps.setPacks(pInfos);
		return lps;
	}


	/**Returns a LevelPackScreen using the given packs, without associated JSON.*/
	public static LevelPackScreen fromPacks(LevelPack[] packs) {
		LevelPackScreen lps = new LevelPackScreen();
		lps.setup();
		lps.setPacks(packs);
		return lps;
	}


	@Override
	protected ScreenController makeController() {
		return new LevelPackScreen.Controller();
	}


	// ===============================================================
	// ========== LevelPackScreen LAYOUT & APPEARANCE ================
	// ===============================================================


	@Override
	protected void addComponents(Container pane) {

		// TODO: an author should be able to edit everything except the
		// OriginalAuthor.
		_InfoPnl = new JPanel();
		_InfoPnl.setLayout(new BoxLayout(_InfoPnl, BoxLayout.Y_AXIS));
		refreshInfoDisplay(_InfoPnl);


		JPanel packInfoBttns = new JPanel();
		packInfoBttns.add(UIBuilder.buildButton().image("icons/application.png").toolTip("Create a new Pack.")
				.action("NEW_LEVELPACK", getController()).focusable(false).create());
		packInfoBttns.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Layout the left-side LevelPack info stuff.
		JPanel packPnl = new JPanel(new BorderLayout());
		packPnl.add(_InfoPnl, BorderLayout.CENTER);
		packPnl.add(packInfoBttns, BorderLayout.PAGE_END);
		packPnl.setBorder(new EmptyBorder(10, 10, 10, 10));


		// Layout the world tree list stuff on the right.
		DefaultTreeModel dtm = new DefaultTreeModel(null);
		dtm.addTreeModelListener((TreeModelListener) getController());
		_Tree = new JTree(dtm);
		_Tree.setRootVisible(false);
		_Tree.setCellRenderer(_TreeRenderer);
		_Tree.setEditable(false);
		_Tree.setBorder(new EmptyBorder(10, 10, 10, 10));
		_Tree.setExpandsSelectedPaths(true);
		_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		_Tree.addTreeSelectionListener((TreeSelectionListener) getController());
		

		JPanel treeBttns = new JPanel();
		treeBttns.add(
				_BttnEditLevel = UIBuilder.buildButton().image("icons/modify.png").action("EDIT_WORLD", getController())
						.focusable(false).toolTip("Open this world in the editor.").create());
		treeBttns.add(_BttnRemoveWorld = UIBuilder.buildButton().image("icons/delete.png")
				.action("REMOVE_WORLD", getController()).focusable(false).toolTip("Remove this world from this pack.")
				.create());
		treeBttns.add(
				_BttnAddWorld = UIBuilder.buildButton().image("icons/add.png").action("ADD_NEW_WORLD", getController())
						.focusable(false).toolTip("Add a new world to this pack.").create());
		treeBttns.add(_BttnWorldUp = UIBuilder.buildButton().image("icons/up.png").action("WORLD_UP", getController())
				.focusable(false).toolTip("Move this world up one slot.").create());
		treeBttns.add(
				_BttnWorldDown = UIBuilder.buildButton().image("icons/down.png").action("WORLD_DOWN", getController())
						.focusable(false).toolTip("Move this world down one slot.").create());
		treeBttns.add(_BttnPlayLevel = UIBuilder.buildButton().image("icons/play.png").toolTip("Play this world.")
				.action("PLAY_LEVEL", getController()).focusable(true).create());

		treeBttns.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel worldPanel = new JPanel(new BorderLayout());
		worldPanel.add(new JScrollPane(_Tree), BorderLayout.CENTER);
		worldPanel.add(treeBttns, BorderLayout.PAGE_END);


		this.setLayout(new BorderLayout());
		this.add(packPnl, BorderLayout.LINE_START);
		this.add(worldPanel, BorderLayout.CENTER);
	}


	private void refreshInfoDisplay(JPanel panel) {
		Object selection = getCurrentSelection();

		ArrayList<Component> list = new ArrayList<Component>();
		if (selection == null) {
			// Top, left, bottom, right
			JLabel lblEmblem = UIBuilder.buildLabel()
					.image(UIBuilder.getImage("icons/compass.png").getScaledInstance(100, 100, Image.SCALE_FAST))
					.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
							new EmptyBorder(60, 110, 60, 110)))
					.create();
			list.add(lblEmblem);
			list.add(UIBuilder.buildLabel().text("Choose a Level Pack or Level to get started.").create());
		} else if (selection instanceof WorldInfo) {
			WorldInfo info = (WorldInfo) selection;
			boolean asAuthor = info.packInfo.getPack().isAuthor(DungeonBotsMain.instance.getUser())
					|| !info.packInfo.getPack().getLocked();
			if (asAuthor) {
				JButton bttnEmblem = UIBuilder.buildButton().image(info.getEmblem())
						.toolTip("Click to change emblem for this level.")
						.action("CHANGE_LEVEL_EMBLEM", getController())
						.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
								new EmptyBorder(10, 10, 10, 10)))
						.create();
				list.add(bttnEmblem);
			} else {
				JLabel lblEmblem = UIBuilder.buildLabel().image(info.getEmblem())
						.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
								new EmptyBorder(10, 10, 10, 10)))
						.create();
				list.add(lblEmblem);
			}
			list.add(Box.createVerticalStrut(10));
			list.add(createDisplayLine("Level Title", info.title, asAuthor));
			list.add(createDisplayLine("Description", info.description, asAuthor));
		} else if (selection instanceof PackInfo) {
			PackInfo info = (PackInfo) selection;
			boolean asAuthor = info.getPack().isAuthor(DungeonBotsMain.instance.getUser())
					|| !info.getPack().getLocked();
			if (asAuthor) {
				JButton bttnEmblem = UIBuilder.buildButton().image(info.getEmblem())
						.toolTip("Click to change emblem for this Level Pack.")
						.action("CHANGE_PACK_EMBLEM", getController())
						.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
								new EmptyBorder(10, 10, 10, 10)))
						.create();
				list.add(bttnEmblem);
			} else {
				JLabel lblEmblem = UIBuilder.buildLabel().image(info.getEmblem())
						.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
								new EmptyBorder(10, 10, 10, 10)))
						.create();
				list.add(lblEmblem);
			}
			list.add(Box.createVerticalStrut(10));
			list.add(createDisplayLine("Pack Title", info.name, asAuthor));
			// Original author can't change.
			list.add(createDisplayLine("Author",
					(info.originalAuthor == null) ? LevelPack.UNKNOWN_AUTHOR_NAME : info.originalAuthor.getUserName(),
					false));
			list.add(createDisplayLine("Description", info.description, asAuthor));
			list.add(Box.createVerticalStrut(10));
			// Original creation date cannot change.
			list.add(createDisplayLine("Created", info.creationDate, false));
			list.add(createDisplayLine("Published", info.publishDate, asAuthor));
			list.add(createDisplayLine("Expires", info.expireDate, asAuthor));
			list.add(Box.createVerticalStrut(10));
			list.add(createDisplayLine("Feedback", info.feedbackModel, asAuthor));
			list.add(Box.createVerticalStrut(10));
			list.add(UIBuilder.buildButton().text("Transitions").action("TRANSITION_SCRIPT", getController())
					.maxWidth(80).focusable(false).create());
		}

		panel.setLayout(new VerticalLayout());
		panel.removeAll();
		for (Component c : list)
			panel.add(c);
		panel.revalidate();
		panel.repaint();
	}


	private Component createDisplayLine(String name, Object initialContents, boolean asAuthor) {


		if (initialContents instanceof LocalDateTime) {
			LocalDateTime dateTime = (LocalDateTime) initialContents;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			if (asAuthor) {
				java.util.Date d = Date.from(dateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
				JPanel pnl = new JPanel(new BorderLayout());
				pnl.add(UIBuilder.buildLabel().text(name + ": ").foreground(Color.black).create(),
						BorderLayout.LINE_START);
				// TODO: This JXDatePicker is utterly invisible on my screen.
				// TODO: JXDatePicker uses the deprecated version of Date.
				JXDatePicker picker = new JXDatePicker();
				picker.setName("JXDatePicker: " + name);
				picker.setDate(d);
				picker.addActionListener(getController());
				pnl.add(picker, BorderLayout.CENTER);
				return pnl;

			} else
				return UIBuilder.buildLabel().text(name + ": " + dateTime.format(formatter)).foreground(Color.BLACK)
						.create();

		} else if (initialContents instanceof String && asAuthor) {
			JPanel pnl = new JPanel(new BorderLayout());
			pnl.add(UIBuilder.buildLabel().text(name + ": ").foreground(Color.BLACK).create(), BorderLayout.LINE_START);
			JTextField field = new JTextField();
			field.setText(initialContents.toString());
			field.setName(name);

			// The listener must be spelled out here, because there is no
			// reference to the JTextField in the document that actually fires
			// the events.
			field.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent e) {
					LevelPackScreen.Controller ctrlr = (LevelPackScreen.Controller) getController();
					ctrlr.handleFieldChange(field, e);
				}


				@Override
				public void insertUpdate(DocumentEvent e) {
					LevelPackScreen.Controller ctrlr = (LevelPackScreen.Controller) getController();
					ctrlr.handleFieldChange(field, e);
				}


				@Override
				public void removeUpdate(DocumentEvent e) {
					LevelPackScreen.Controller ctrlr = (LevelPackScreen.Controller) getController();
					ctrlr.handleFieldChange(field, e);
				}
			});
			pnl.add(field, BorderLayout.CENTER);
			pnl.setMaximumSize(new Dimension(9999, 25));
			return pnl;
		} else if (initialContents instanceof LevelPack.FeedbackModel && asAuthor) {
			JPanel pnl = new JPanel(new VerticalLayout());
			pnl.add(new JLabel("Feedback model:"));
			LevelPack.FeedbackModel[] models = LevelPack.FeedbackModel.values();
			ButtonGroup group = new ButtonGroup();
			for (int i = 0; i < models.length; i++) {
				LevelPack.FeedbackModel m = models[i];
				JRadioButton bttn = new JRadioButton(m.toString());
				bttn.setSelected(m.equals(initialContents));
				group.add(bttn);
				pnl.add(bttn);
				bttn.setName(m.toString());
				bttn.setActionCommand("CHANGE_FEEDBACK_MODEL");
				bttn.addActionListener(getController());
			}
			return pnl;

		} else
			return UIBuilder.buildLabel().text(name + ": " + initialContents.toString()).foreground(Color.BLACK)
					.create();

	}


	@Override
	protected void setDefaultLayout() {
		this.setSize(1024, 640);
		this.setLocationRelativeTo(null);
		this.setUndecorated(false);
		this.setTitle("Choose your adventure.");
	}


	private DefaultMutableTreeNode getRootNode() {
		if (_Tree == null)
			return null;
		TreeModel m = _Tree.getModel();
		if (m == null)
			return null;
		return (DefaultMutableTreeNode) m.getRoot();
	}


	/**Collapses all open LevelPacks.*/
	public void collapseAll() {
		int i = 0;
		while (i < _Tree.getRowCount())
			_Tree.collapseRow(i++);
	}


	/**Expands all LevelPacks.*/
	public void expandAll() {
		int i = 0;
		while (i < _Tree.getRowCount())
			_Tree.expandRow(i++);
	}


	// ===========================================================
	// ========== LevelPackScreen SELECTION STUFF ================
	// ===========================================================


	/**Returns the object that is currently selected.  If there is no selection, returns null.*/
	private Object getCurrentSelection() {
		if (_Tree == null)
			return null;
		TreePath path = _Tree.getSelectionPath();
		if (path == null)
			return null;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		return node.getUserObject();
	}


	/**Sets the selection to the first world if there is one, or if not to the first LevelPack 
	 * if there is one.  If not, sets selection to null.*/
	private void selectFirst() {
		TreeNode root = getRootNode();
		if (root != null && root.getChildCount() > 0) {
			DefaultMutableTreeNode packNode = (DefaultMutableTreeNode) root.getChildAt(0);
			setSelection(((PackInfo) packNode.getUserObject()).getPack(), 0);
		}
	}


	/**Sets the current selection to the given LevelPack.  If the given LevelPack does not appear 
	 * in the tree, sets selection to null.*/
	public void setSelection(LevelPack pack) {
		TreePath path = getPath(pack);
		_Tree.setSelectionPath(path);
	}


	/**Sets the current selection to the indicated world within the given LevelPack.  Note that if the
	 * world does not exist at the given index, the given LevelPack will be selected.  If the given 
	 * LevelPack doesn't exist in the tree, sets selection to null.*/
	public void setSelection(LevelPack pack, int index) {
		TreePath path = getPath(pack);
		if (path == null) {
			_Tree.setSelectionPath(null);
			return;
		}
		TreeNode n = (TreeNode) path.getLastPathComponent();
		if (index < n.getChildCount())
			path = path.pathByAddingChild(n.getChildAt(index));
		_Tree.setSelectionPath(path);
	}


	// ================================================================
	// ========== LevelPackScreen LEVELPACK MANAGEMENT ================
	// ================================================================

	/**Sets the contents of this LevelPackScreen to the indicated packs.  No JSON will be 
	 * stored.*/
	public void setPacks(LevelPack[] packs) {
		PackInfo[] pInfos = new PackInfo[packs.length];
		for (int i = 0; i < packs.length; i++)
			pInfos[i] = PackInfo.withoutJSON(packs[i]);
		setPacks(pInfos);
	}


	/**Sets the contents of this LevelPackScreen to the indicated PackInfos.*/
	private void setPacks(PackInfo[] packs) {

		// Clear what already exists.
		if (_Tree == null)
			setup();
		_Tree.removeAll();

		// Build the tree.
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
		for (int i = 0; i < packs.length; i++) {
			PackInfo pInfo = packs[i];
			pInfo.worlds.clear();
			DefaultMutableTreeNode packNode = new DefaultMutableTreeNode(pInfo);
			rootNode.add(packNode);
			LevelPack pack = pInfo.getPack();
			for (int j = 0; j < pack.getLevelCount(); j++) {
				WorldInfo wInfo = WorldInfo.fromLevelIndex(pInfo, j);
				pInfo.worlds.add(wInfo);
				DefaultMutableTreeNode worldNode = new DefaultMutableTreeNode(wInfo);
				packNode.add(worldNode);
			}
		}

		// Set the model and the first selection. Note, this will not work until
		// the full screen (and the tree) has been validated.
		DefaultTreeModel m = new DefaultTreeModel(rootNode);
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				m.addTreeModelListener((TreeModelListener)getController());
				_Tree.setModel(m);
				selectFirst();
			}
		});


	}


	/**Returns the path to the given LevelPack.*/
	private TreePath getPath(LevelPack pack) {
		DefaultMutableTreeNode root = getRootNode();
		TreePath path = new TreePath(root);
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
			PackInfo pi = (PackInfo) child.getUserObject();
			if (pi.getPack().equals(pack)) {
				return path.pathByAddingChild(child);
			}
		}
		return null;
	}


	/**Gets all the LevelPack files in the given directory.*/
	private static File[] getPackFiles(String directory) {
		File dir = new File(directory);
		File[] allFiles = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (!f.isFile())
					return false;
				return f.getName().toLowerCase().endsWith("." + LevelPack.EXTENSION.toLowerCase());
			}
		});
		return allFiles;
	}


	/**Returns the number of LevelPacks.*/
	public int getLevelPackCount() {
		TreeNode root = getRootNode();
		return root.getChildCount();
	}


	/**Returns the LevelPack at the given index number, or null if the number is invalid.*/
	public LevelPack getLevelPackAt(int index) {
		DefaultMutableTreeNode root = getRootNode();
		if (index >= root.getChildCount())
			return null;
		DefaultMutableTreeNode packNode = (DefaultMutableTreeNode) root.getChildAt(index);
		return ((PackInfo) packNode.getUserObject()).getPack();
	}


	/**Returns the complete, non-partial, selected LevelPack.  The LevelPack's current world will be set appropriately.*/
	public LevelPack getSelectedLevelPack() {
		throw new RuntimeException("Not implemented yet.");
	}


	// ===============================================================
	// ========== LevelPackScreen EDITING STUFF ======================
	// ===============================================================

	private Undoable<DefaultMutableTreeNode> addNewWorld() {
		TreePath path = _Tree.getSelectionPath();
		PackInfo pInfo = null;
		DefaultMutableTreeNode nodePack = null;
		do {
			nodePack = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (nodePack.getUserObject() instanceof PackInfo)
				pInfo = (PackInfo) nodePack.getUserObject();
			else
				path = path.getParentPath();
		} while (pInfo == null);

		WorldInfo newWorld = WorldInfo.fromNew(pInfo, "New level", "(No description)",
				UIBuilder.getImage(LevelPack.DEFAULT_LEVEL_EMBLEM_FILE));
		pInfo.worlds.add(newWorld);
		DefaultTreeModel model = (DefaultTreeModel)_Tree.getModel();
		DefaultMutableTreeNode nodeNew = new DefaultMutableTreeNode(newWorld);
		model.insertNodeInto(nodeNew,  nodePack,  nodePack.getChildCount());
		
		_Tree.revalidate();
		_Tree.repaint();

		Undoable<DefaultMutableTreeNode> u = new Undoable<DefaultMutableTreeNode>(null, nodeNew, nodeNew.getParent()) {

			@Override
			protected boolean okayToUndo() {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) context;
				if (!after.getParent().equals(parent))
					return false;
				if (!parent.isNodeChild(after))
					return false;
				WorldInfo wInfo = (WorldInfo) after.getUserObject();
				if (!wInfo.packInfo.worlds.contains(wInfo))
					return false;
				return true;
			}


			@Override
			protected void undoValidated() {
				WorldInfo wInfo = (WorldInfo) after.getUserObject();
				wInfo.packInfo.worlds.remove(wInfo);
				after.removeFromParent();
				after.setParent(null);
			}


			@Override
			protected boolean okayToRedo() {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) context;
				if (after.getParent() != null)
					return false;
				if (parent.isNodeChild(after))
					return false;
				WorldInfo wInfo = (WorldInfo) after.getUserObject();
				if (wInfo.packInfo.worlds.contains(wInfo))
					return false;
				return true;
			}


			@Override
			protected void redoValidated() {
				WorldInfo wInfo = (WorldInfo) after.getUserObject();
				wInfo.packInfo.worlds.add(wInfo);
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) context;
				parent.add(after);
				after.setParent(parent);
			}

		};

		return u;

	}


	private Undoable<String> changePackName(String newName) {
		PackInfo pInfo = (PackInfo) getCurrentSelection();
		pInfo.hasChanged = true;
		String oldName = pInfo.name;
		pInfo.name = newName;
		return new Undoable<String>(oldName, newName, pInfo) {

			@Override
			protected boolean okayToUndo() {
				PackInfo info = (PackInfo) context;
				return info.name.equals(after);
			}


			@Override
			protected boolean okayToRedo() {
				PackInfo info = (PackInfo) context;
				return info.name.equals(before);
			}


			@Override
			protected void undoValidated() {
				PackInfo info = (PackInfo) context;
				info.name = before;
			}


			@Override
			protected void redoValidated() {
				PackInfo info = (PackInfo) context;
				info.name = after;

			}

		};
	}


	private Undoable<String> changePackDescription(String newDescription) {
		PackInfo pInfo = (PackInfo) getCurrentSelection();
		pInfo.hasChanged = true;
		String oldDescription = pInfo.description;
		pInfo.description = newDescription;
		return new Undoable<String>(oldDescription, newDescription, pInfo) {

			@Override
			protected boolean okayToUndo() {
				PackInfo info = (PackInfo) context;
				return info.description.equals(after);
			}


			@Override
			protected boolean okayToRedo() {
				PackInfo info = (PackInfo) context;
				return info.description.equals(before);
			}


			@Override
			protected void undoValidated() {
				PackInfo info = (PackInfo) context;
				info.description = before;
			}


			@Override
			protected void redoValidated() {
				PackInfo info = (PackInfo) context;
				info.description = after;

			}

		};
	}


	private Undoable<LevelPack.FeedbackModel> changeFeedbackModel(LevelPack.FeedbackModel model) {
		PackInfo pInfo = (PackInfo) getCurrentSelection();
		pInfo.hasChanged = true;
		LevelPack.FeedbackModel oldModel = pInfo.feedbackModel;
		LevelPack.FeedbackModel newModel = model;
		pInfo.feedbackModel = model;
		return new Undoable<LevelPack.FeedbackModel>(oldModel, newModel, pInfo) {

			@Override
			protected boolean okayToUndo() {
				PackInfo info = (PackInfo) context;
				return info.feedbackModel.equals(after);
			}


			@Override
			protected boolean okayToRedo() {
				PackInfo info = (PackInfo) context;
				return info.feedbackModel.equals(before);
			}


			@Override
			protected void undoValidated() {
				PackInfo info = (PackInfo) context;
				info.feedbackModel = before;
			}


			@Override
			protected void redoValidated() {
				PackInfo info = (PackInfo) context;
				info.feedbackModel = after;
			}

		};
	}


	private Undoable<Image> changePackImage() {
		File file = FileControl.openDialog(LevelPackScreen.this);
		Image img = UIBuilder.getImage(file.getPath(), true);
		if (img == null) {
			JOptionPane.showMessageDialog(LevelPackScreen.this, "Cannot load the given image:" + file.getPath());
			return null;
		}
		PackInfo pInfo = (PackInfo) getCurrentSelection();
		pInfo.hasChanged = true;
		Image oldImg = pInfo.getEmblem();
		Image newImg = img;
		pInfo.setEmblem(newImg);
		refreshInfoDisplay(_InfoPnl);
		_Tree.repaint();
		return new Undoable<Image>(oldImg, pInfo.getEmblem(), pInfo) {

			@Override
			protected boolean okayToUndo() {
				PackInfo info = (PackInfo) context;
				return (info.getEmblem().equals(after));
			}


			@Override
			protected boolean okayToRedo() {
				PackInfo info = (PackInfo) context;
				return (info.getEmblem().equals(before));
			}


			@Override
			protected void undoValidated() {
				PackInfo info = (PackInfo) context;
				info.setEmblem(before);
			}


			@Override
			protected void redoValidated() {
				PackInfo info = (PackInfo) context;
				info.setEmblem(after);
			}
		};
	}


	private Undoable<Image> changeLevelImage() {
		File file = FileControl.openDialog(LevelPackScreen.this);
		Image img = UIBuilder.getImage(file.getPath(), true);
		if (img == null) {
			JOptionPane.showMessageDialog(LevelPackScreen.this, "Cannot load the given image:" + file.getPath());
			return null;
		}
		WorldInfo wInfo = (WorldInfo) getCurrentSelection();
		wInfo.packInfo.hasChanged = true;
		Image oldImg = wInfo.getEmblem();
		Image newImg = img;
		wInfo.setEmblem(newImg);
		refreshInfoDisplay(_InfoPnl);
		_Tree.repaint();
		return new Undoable<Image>(oldImg, wInfo.getEmblem(), wInfo) {

			@Override
			protected boolean okayToUndo() {
				WorldInfo info = (WorldInfo) context;
				return (info.getEmblem().equals(after));
			}


			@Override
			protected boolean okayToRedo() {
				WorldInfo info = (WorldInfo) context;
				return (info.getEmblem().equals(before));
			}


			@Override
			protected void undoValidated() {
				WorldInfo info = (WorldInfo) context;
				info.setEmblem(before);
			}


			@Override
			protected void redoValidated() {
				WorldInfo info = (WorldInfo) context;
				info.setEmblem(after);
			}
		};
	}


	private Undoable<String> changeLevelDescription(String newDescription) {
		WorldInfo wInfo = (WorldInfo) getCurrentSelection();
		wInfo.packInfo.hasChanged = true;
		String oldDescription = wInfo.description;
		wInfo.description = newDescription;
		return new Undoable<String>(oldDescription, newDescription, wInfo) {

			@Override
			protected boolean okayToUndo() {
				PackInfo info = (PackInfo) context;
				return info.description.equals(after);
			}


			@Override
			protected boolean okayToRedo() {
				PackInfo info = (PackInfo) context;
				return info.description.equals(before);
			}


			@Override
			protected void undoValidated() {
				PackInfo info = (PackInfo) context;
				info.description = before;
			}


			@Override
			protected void redoValidated() {
				PackInfo info = (PackInfo) context;
				info.description = after;

			}

		};
	}


	private Undoable<String> changeLevelTitle(String newTitle) {
		WorldInfo wInfo = (WorldInfo) getCurrentSelection();
		wInfo.packInfo.hasChanged = true;
		String oldTitle = wInfo.description;
		wInfo.title = newTitle;
		return new Undoable<String>(oldTitle, newTitle, wInfo) {

			@Override
			protected boolean okayToUndo() {
				WorldInfo info = (WorldInfo) context;
				return info.title.equals(after);
			}


			@Override
			protected boolean okayToRedo() {
				WorldInfo info = (WorldInfo) context;
				return info.title.equals(before);
			}


			@Override
			protected void undoValidated() {
				WorldInfo info = (WorldInfo) context;
				info.title = before;
			}


			@Override
			protected void redoValidated() {
				WorldInfo info = (WorldInfo) context;
				info.title = after;

			}

		};
	}


	// ===============================================================
	// ========== LevelPackScreen HELPER CLASSES =====================
	// ===============================================================


	/**A data structure that associates a LevelPack with its original JSON String.  This is useful because the tree list 
	 * cannot fully deserialize every LevelPack, it would take too long.  */
	private static final class PackInfo {

		private final LevelPack _pack;
		private final String originalJson;
		public final ArrayList<WorldInfo> worlds = new ArrayList<WorldInfo>();
		public boolean hasChanged = false;

		private Image _emblem;
		private Image thumbnail;
		public String name;
		public final User originalAuthor;
		public String description;
		public final LocalDateTime creationDate;
		public LocalDateTime publishDate;
		public LocalDateTime expireDate;
		public int levelCount;
		public LevelPack.FeedbackModel feedbackModel;


		/**Returns a new PackInfo, associated with the given JSON string.*/
		public static PackInfo withJSON(LevelPack pack, String json) {
			return new PackInfo(pack, json);
		}


		public static PackInfo withoutJSON(LevelPack pack) {
			return new PackInfo(pack, null);
		}


		private PackInfo(LevelPack pack, String json) {
			this._pack = pack;
			this.originalJson = json;
			setEmblem(pack.getEmblem());
			this.name = pack.getName();
			this.originalAuthor = pack.getOriginalAuthor();
			this.description = pack.getDescription();
			this.creationDate = pack.getCreationDate();
			this.publishDate = pack.getPublishStart();
			this.expireDate = pack.getPublishEnd();
			this.levelCount = pack.getLevelCount();
			this.feedbackModel = pack.getFeedbackModel();
		}


		public LevelPack getPack() {
			return _pack;
		}


		/**Gets the current emblem.  You can't get the emblem field directly because have to make sure 
		 * the thumbnail was updated, and the image has to be correctly scaled..*/
		public Image getEmblem() {
			return _emblem;
		}


		/**The thumbnail version of the emblem, cached for performance.*/
		public Image getThumbnail() {
			return thumbnail;
		}


		/**Sets the current emblem.  You can't set the emblem field directly because have to make sure 
		 * the thumbnail was updated, and the image has to be correctly scaled.*/
		public void setEmblem(Image image) {
			_emblem = image.getScaledInstance(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT, Image.SCALE_SMOOTH);
			thumbnail = image.getScaledInstance(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Image.SCALE_SMOOTH);
		}


		/**Writes the PackInfo's stored data to the referenced LevelPack.  The resulting LevelPack will 
		 * be completely deserialized, ready to play.*/
		public LevelPack write() {

			// The result will either start out as a new LevelPack created in
			// this screen, or a LevelPack completely deserialized from the
			// JSON. Either way, must ensure the collection of Worlds is in the
			// correct state, fully deserialized.
			LevelPack pack;
			if (this.originalJson != null) {
				pack = LevelPack.fromJson(originalJson);
				// Now, figure out how the Worlds have changed. There can be
				// insertions, deletions, etc.

				// Determine the levels in the correct order.
				World[] completeWorlds = new World[this.worlds.size()];
				Image[] emblems = new Image[this.worlds.size()];
				String[] titles = new String[this.worlds.size()];
				String[] descriptions = new String[this.worlds.size()];
				for (int i = 0; i < this.worlds.size(); i++) {
					WorldInfo wInfo = this.worlds.get(i);
					if (wInfo.originalIndex < 0) 
						completeWorlds[i] = new World(new File(LevelPack.DEFAULT_WORLD_FILE), wInfo.title);
					 else 
						completeWorlds[i] = pack.getWorld(wInfo.originalIndex);
					
					completeWorlds[i].setName(wInfo.title);
					emblems[i] = wInfo.getEmblem();
					titles[i] = wInfo.title;
					descriptions[i] = wInfo.description;
				}

				// Now, simply assign the levels and their header
				// characteristics.
				pack.setWorlds(completeWorlds);
				pack.setLevelEmblems(emblems);
				pack.setLevelTitles(titles);
				pack.setLevelDescriptions(descriptions);

			} else {
				pack = new LevelPack(this.name, this.originalAuthor,
						this.worlds.toArray(new World[this.worlds.size()]));
				World[] completeWorlds = new World[this.worlds.size()];
				Image[] emblems = new Image[this.worlds.size()];
				String[] titles = new String[this.worlds.size()];
				String[] descriptions = new String[this.worlds.size()];
				for (int i = 0; i < this.worlds.size(); i++) {
					WorldInfo wInfo = this.worlds.get(i);
					completeWorlds[i] = new World(new File(LevelPack.DEFAULT_WORLD_FILE), wInfo.title);
					emblems[i] = wInfo.getEmblem();
					titles[i] = wInfo.title;
					descriptions[i] = wInfo.description;
				}
				pack.setWorlds(completeWorlds);
				pack.setLevelEmblems(emblems);
				pack.setLevelTitles(titles);
				pack.setLevelDescriptions(descriptions);
			}


			// Now, write the particulars of the level pack.
			pack.setEmblem(this.getEmblem());
			pack.setName(this.name);
			pack.setDescription(this.description);
			pack.setPublicationStart(this.publishDate);
			pack.setPublicationEnd(this.expireDate);
			pack.setFeedbackModel(this.feedbackModel);

			return pack;
		}
	}


	/**A data structure that embodies the "partial" deserialization of a World/level, and associates it with its original 
	 * LevelPack and index.  This is useful because the tree list cannot fully deserialize every World in every 
	 * LevelPack, it would just take too long.*/
	private static final class WorldInfo {

		public String title;
		public String description;
		private Image _emblem;
		private Image thumbnail;
		private final PackInfo packInfo;
		private final int originalIndex;


		private WorldInfo(PackInfo packInfo, String title, String description, Image emblem, int index) {
			this.packInfo = packInfo;
			this.title = title;
			this.description = description;
			setEmblem(emblem);
			this.originalIndex = index;
		}


		/**Gets the current emblem.  You can't get the emblem field directly because have to make sure 
		 * the thumbnail was updated, and the image has be correctly scaled.*/
		public Image getEmblem() {
			return _emblem;
		}


		/**The thumbnail version of the emblem, cached for performance.*/
		public Image getThumbnail() {
			return thumbnail;
		}


		/**Sets the current emblem.  You can't set the emblem field directly because have to make sure 
		 * the thumbnail was updated, and the image has be correctly scaled.*/
		public void setEmblem(Image image) {
			_emblem = image.getScaledInstance(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT, Image.SCALE_SMOOTH);
			thumbnail = image.getScaledInstance(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Image.SCALE_SMOOTH);
		}


		/**Reads only enough of the level's basic info to produce a WorldInfo.*/
		public static WorldInfo fromLevelIndex(PackInfo packInfo, int index) {
			LevelPack pack = packInfo.getPack();
			String title = pack.getLevelTitle(index);
			String desc = pack.getLevelDescription(index);
			Image img = pack.getLevelEmblem(index);
			WorldInfo wi = new WorldInfo(packInfo, title, desc, img, index);
			return wi;
		}


		public static WorldInfo fromNew(PackInfo info, String title, String description, Image emblem) {
			return new WorldInfo(info, title, description, emblem, -1);
		}


	}


	private final TreeCellRenderer _TreeRenderer = new TreeCellRenderer() {

		private final EmptyBorder spacer = new EmptyBorder(2, 2, 2, 2);


		@Override
		public final Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
				boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
			if (value instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				if (node.getUserObject() instanceof PackInfo) {
					return createPackComponent((PackInfo) node.getUserObject(), isSelected, isExpanded, isLeaf, row,
							hasFocus);
				} else if (node.getUserObject() instanceof WorldInfo) {
					// The level should have its row index within the context of
					// the parent.
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
					return createLevelComponent((WorldInfo) node.getUserObject(), isSelected, isExpanded, isLeaf,
							parent.getIndex(node), hasFocus);
				} else if (node.getUserObject() instanceof String) {
					return new JLabel(node.getUserObject().toString());
				}

			}
			throw new RuntimeException("Sanity check.  This shouldn't happen.");
		}


		/**Returns a visual component for displaying a LevelPack within the tree.*/
		private final Component createPackComponent(PackInfo pInfo, boolean isSelected, boolean isExpanded,
				boolean isLeaf, int row, boolean hasFocus) {
			JPanel pnl = new JPanel();
			pnl.setOpaque(false);
			pnl.setLayout(new HorizontalLayout());
			pnl.setBorder(new EmptyBorder(2, 2, 2, 2));
			pnl.add(UIBuilder.buildLabel().image(pInfo.getThumbnail()).border(new EmptyBorder(2, 2, 2, 2)).create());
			pnl.add(UIBuilder.buildLabel().text(pInfo.name + " - ").border(spacer).create());
			pnl.add(UIBuilder.buildLabel().text(pInfo.description).border(new EmptyBorder(2, 2, 2, 2)).create());
			String author = pInfo.originalAuthor == null ? LevelPack.UNKNOWN_AUTHOR_NAME
					: pInfo.originalAuthor.getUserName();
			pnl.add(UIBuilder.buildLabel().text("by " + author).border(new EmptyBorder(2, 2, 2, 2)).create());
			return pnl;

		}


		/**Returns a visual component for displaying a World within the tree.*/
		private final Component createLevelComponent(WorldInfo info, boolean isSelected, boolean isExpanded,
				boolean isLeaf, int row, boolean hasFocus) {
			JPanel pnl = new JPanel();
			pnl.setOpaque(false);
			pnl.setLayout(new HorizontalLayout());
			pnl.setBorder(new EmptyBorder(2, 2, 2, 2));
			pnl.add(UIBuilder.buildLabel().image(info.getThumbnail()).border(new EmptyBorder(2, 2, 2, 2)).create());
			pnl.add(UIBuilder.buildLabel().text(info.title).border(new EmptyBorder(2, 2, 2, 2)).create());
			pnl.add(UIBuilder.buildLabel().text(info.description).border(new EmptyBorder(2, 2, 2, 2)).create());
			return pnl;
		}
	};


	private final class Controller extends ScreenController implements TreeModelListener, TreeSelectionListener {


		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "CHANGE_LEVEL_EMBLEM":
				Undoable<Image> levelEmblemChange = changeLevelImage();
				if (levelEmblemChange != null)
					_UndoStack.push(levelEmblemChange);
				return;
			case "CHANGE_PACK_EMBLEM":
				Undoable<Image> packEmblemChange = changePackImage();
				if (packEmblemChange != null)
					_UndoStack.push(packEmblemChange);
				return;
			case "CHANGE_FEEDBACK_MODEL":
				JRadioButton source = (JRadioButton) e.getSource();
				LevelPack.FeedbackModel model = LevelPack.FeedbackModel.valueOf(source.getName());
				Undoable<LevelPack.FeedbackModel> feedbackModelChange = changeFeedbackModel(model);
				if (feedbackModelChange != null)
					_UndoStack.push(feedbackModelChange);
				return;
			case "PLAY_LEVEL":
				WorldInfo selection = (WorldInfo) getCurrentSelection();
				LevelPack newPack = selection.packInfo.write();
				newPack.setCurrentWorld(selection.packInfo.worlds.indexOf(selection));
				DungeonBotsMain.instance.setCurrentScreen(new GameplayScreen(newPack));
				break;
			case "ADD_NEW_WORLD":
				Undoable<DefaultMutableTreeNode> addWorldChange = addNewWorld();
				if (addWorldChange != null)
					_UndoStack.push(addWorldChange);
				return;
			default:
				System.out.println(this.getClass().getName() + " has not implemented command: " + e.getActionCommand());
			}

		}


		/**Handles a changed field in the GUI, applying the change to the appropriate WorldInfo or PackInfo.*/
		public void handleFieldChange(JTextField field, DocumentEvent e) {

			Object context = getCurrentSelection();

			if (context instanceof WorldInfo) {
				switch (field.getName().toLowerCase()) {
				case "level title":
					Undoable<String> titleChange = changeLevelTitle(field.getText());
					if (titleChange != null)
						_UndoStack.push(titleChange);
					return;
				case "description":
					Undoable<String> descChange = changeLevelDescription(field.getText());
					if (descChange != null)
						_UndoStack.push(descChange);
					return;
				}
			} else if (context instanceof PackInfo) {
				switch (field.getName().toLowerCase()) {
				case "pack title":
					Undoable<String> nameChange = changePackName(field.getText());
					if (nameChange != null)
						_UndoStack.push(nameChange);
					return;
				case "description":
					Undoable<String> descChange = changePackDescription(field.getText());
					if (descChange != null)
						_UndoStack.push(descChange);
					return;
				}
			}
			System.out.println("Have not implemented LevelPackScreen.Controller.handleFieldChange() for field "
					+ field.getName() + " in context " + context.toString());

		}


		/**Called when the tree's selection changes.*/
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			refreshInfoDisplay(_InfoPnl);
			Object selection = getCurrentSelection();
			if (selection == null) {
				_BttnPlayLevel.setEnabled(false);
				_BttnEditLevel.setEnabled(false);
				_BttnWorldUp.setEnabled(false);
				_BttnWorldDown.setEnabled(false);
				_BttnAddWorld.setEnabled(false);
				_BttnRemoveWorld.setEnabled(false);
			} else {
				_BttnPlayLevel.setEnabled(selection instanceof WorldInfo);
				_BttnEditLevel.setEnabled(selection instanceof WorldInfo);
				_BttnWorldUp.setEnabled(selection instanceof WorldInfo
						&& ((WorldInfo) selection).packInfo.worlds.indexOf(selection) > 0);
				_BttnWorldDown.setEnabled(selection instanceof WorldInfo && ((WorldInfo) selection).packInfo.worlds
						.indexOf(selection) < ((WorldInfo) selection).packInfo.worlds.size() - 1);
				_BttnAddWorld.setEnabled(true);
				_BttnRemoveWorld.setEnabled(true);
			}


		}


		@Override
		public void treeNodesChanged(TreeModelEvent e) {
			System.out.println("treeNodesChanged");
		}


		@Override
		public void treeNodesInserted(TreeModelEvent e) {
			System.out.println("treeNodesInserted");
		}


		@Override
		public void treeNodesRemoved(TreeModelEvent e) {
			System.out.println("treeNodesRemoved");
		}


		@Override
		public void treeStructureChanged(TreeModelEvent e) {
			System.out.println("treeStructureChanged");
		}


	}


}
