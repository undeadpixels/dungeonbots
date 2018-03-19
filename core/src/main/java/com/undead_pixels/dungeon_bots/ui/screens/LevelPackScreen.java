package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
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

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.undo.UndoStack;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;

/**
 * The screen where a user selects what level/pack they want to play
 */
public class LevelPackScreen extends Screen {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTree _Tree = null;
	private JPanel _InfoPnl = null;

	private final UndoStack _UndoStack = new UndoStack();

	/**This is essentially the data model.*/
	private final ArrayList<PackInfo> _Packs = new ArrayList<PackInfo>();
	private final ArrayList<ArrayList<WorldInfo>> _Levels = new ArrayList<ArrayList<WorldInfo>>();


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
		LevelPackScreen lps = fromPacks(goodPacks.toArray(new LevelPack[goodPacks.size()]));

		// Correct the packs to include references to their original JSONs.
		int j = 0;
		while (j < lps._Packs.size()) {
			PackInfo packInfo = new PackInfo(lps._Packs.get(j).pack, goodJsons.get(j));
			lps._Packs.set(j++, packInfo);
		}
		return lps;
	}


	/**Returns a LevelPackScreen using the given packs.*/
	public static LevelPackScreen fromPacks(LevelPack[] packs) {
		LevelPackScreen lps = new LevelPackScreen();
		lps.setPacks(packs);
		lps.setup();
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
		packInfoBttns.add(
				UIBuilder.buildButton().text("Open").action("OPEN_PACK", getController()).focusable(false).create());
		packInfoBttns
				.add(UIBuilder.buildButton().text("New").action("NEW_PACK", getController()).focusable(false).create());
		packInfoBttns.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Layout the left-side LevelPack info stuff.
		JPanel packPnl = new JPanel(new BorderLayout());
		packPnl.add(_InfoPnl, BorderLayout.CENTER);
		packPnl.add(packInfoBttns, BorderLayout.PAGE_END);
		packPnl.setBorder(new EmptyBorder(10, 10, 10, 10));


		// Layout the world tree list stuff on the right.
		DefaultTreeModel dtm = new DefaultTreeModel(createRootNode());
		dtm.addTreeModelListener((TreeModelListener) getController());
		_Tree = new JTree(dtm);
		_Tree.setRootVisible(false);
		_Tree.setCellRenderer(_TreeRenderer);
		_Tree.setEditable(true);
		_Tree.setBorder(new EmptyBorder(10, 10, 10, 10));
		_Tree.setExpandsSelectedPaths(true);
		_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		_Tree.addTreeSelectionListener((TreeSelectionListener) getController());

		JPanel worldBttns = new JPanel();
		worldBttns.add(
				UIBuilder.buildButton().text("Add").action("ADD_WORLD", getController()).focusable(false).create());
		worldBttns.add(
				UIBuilder.buildButton().text("Edit").action("EDIT_WORLD", getController()).focusable(false).create());
		worldBttns.add(UIBuilder.buildButton().text("Remove").action("REMOVE_WORLD", getController()).focusable(false)
				.create());
		worldBttns.add(
				UIBuilder.buildButton().text("Play").action("PLAY_WORLD", getController()).focusable(true).create());
		worldBttns.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel worldPanel = new JPanel(new BorderLayout());
		worldPanel.add(new JScrollPane(_Tree), BorderLayout.CENTER);
		worldPanel.add(worldBttns, BorderLayout.PAGE_END);


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
			boolean asAuthor = info.levelPack.isAuthor(DungeonBotsMain.instance.getUser())
					|| !info.levelPack.getLocked();
			if (asAuthor) {
				JButton bttnEmblem = UIBuilder.buildButton()
						.image(info.emblem.getScaledInstance(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT,
								Image.SCALE_FAST))
						.toolTip("Click to change emblem for this level.")
						.action("CHANGE_LEVEL_EMBLEM", getController())
						.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
								new EmptyBorder(10, 10, 10, 10)))
						.create();
				list.add(bttnEmblem);
			} else {
				JLabel lblEmblem = UIBuilder.buildLabel()
						.image(info.emblem.getScaledInstance(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT,
								Image.SCALE_FAST))
						.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
								new EmptyBorder(10, 10, 10, 10)))
						.create();
				list.add(lblEmblem);
			}
			list.add(Box.createVerticalStrut(10));
			list.add(createDisplayLine("Level", info.title, asAuthor));
			list.add(createDisplayLine("Description", info.title, asAuthor));
		} else if (selection instanceof PackInfo) {
			PackInfo info = (PackInfo) selection;
			boolean asAuthor = info.pack.isAuthor(DungeonBotsMain.instance.getUser()) || !info.pack.getLocked();
			LevelPack pack = info.pack;
			if (asAuthor) {
				JButton bttnEmblem = UIBuilder.buildButton()
						.image(pack.getEmblem().getScaledInstance(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT,
								Image.SCALE_FAST))
						.toolTip("Click to change emblem for this Level Pack.")
						.action("CHANGE_LEVELPACK_EMBLEM", getController())
						.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
								new EmptyBorder(10, 10, 10, 10)))
						.create();
				list.add(bttnEmblem);
			} else {
				JLabel lblEmblem = UIBuilder.buildLabel()
						.image(pack.getEmblem().getScaledInstance(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT,
								Image.SCALE_FAST))
						.border(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
								new EmptyBorder(10, 10, 10, 10)))
						.create();
				list.add(lblEmblem);
			}
			list.add(Box.createVerticalStrut(10));
			list.add(createDisplayLine("LevelPack", pack.getName(), asAuthor));
			list.add(createDisplayLine("Author", pack.getOriginalAuthor(), asAuthor));
			list.add(createDisplayLine("Description", pack.getDescription(), asAuthor));
			list.add(Box.createVerticalStrut(10));
			list.add(createDisplayLine("Created", pack.getCreationDate(), asAuthor));
			list.add(createDisplayLine("Published", pack.getPublishStart(), asAuthor));
			list.add(createDisplayLine("Expires", pack.getPublishEnd(), asAuthor));
			list.add(Box.createVerticalStrut(10));
			list.add(createDisplayLine("Levels", pack.getLevelCount(), asAuthor));
			list.add(createDisplayLine("Feedback", pack.getFeedbackModel(), asAuthor));
			list.add(Box.createVerticalStrut(10));
		}

		panel.removeAll();
		for (Component c : list)
			panel.add(c);
		panel.revalidate();
	}


	/**Create the tree structure to be displayed, from the given LevelPacks.*/
	private DefaultMutableTreeNode createRootNode() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
		for (int i = 0; i < _Packs.size(); i++) {
			PackInfo pi = _Packs.get(i);
			DefaultMutableTreeNode packNode = new DefaultMutableTreeNode(pi);
			rootNode.add(packNode);
			for (int j = 0; j < _Levels.get(i).size(); j++) {
				WorldInfo wi = _Levels.get(i).get(j);
				DefaultMutableTreeNode worldNode = new DefaultMutableTreeNode(wi);
				packNode.add(worldNode);
			}
		}
		return rootNode;
	}


	@Override
	protected void setDefaultLayout() {
		this.setSize(1024, 550);
		this.setLocationRelativeTo(null);
		this.setUndecorated(false);
		this.setTitle("Choose your adventure.");
	}


	private static Component createDisplayLine(String name, Object initialContents, boolean asAuthor) {
		if (!asAuthor) {
			JLabel result = new JLabel(name + ": " + initialContents);
			return result;
		}
		JLabel result = new JLabel(name + ": " + initialContents);
		result.setForeground(Color.BLACK);
		return result;
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
			setSelection(((PackInfo) packNode.getUserObject()).pack, 0);
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

	/**Sets the contents of this LevelPackScreen to the indicated packs.*/
	public void setPacks(LevelPack[] packs) {

		// Build the pack structure.
		_Packs.clear();
		_Levels.clear();
		for (LevelPack pack : packs) {
			_Packs.add(new PackInfo(pack, null));
			ArrayList<WorldInfo> levels = new ArrayList<WorldInfo>();
			_Levels.add(levels);
			for (int i = 0; i < pack.getLevelCount(); i++) {
				WorldInfo wi = WorldInfo.fromLevel(pack, i);
				levels.add(wi);
			}
		}

		// Select the first world in the list.
		selectFirst();
	}


	/**Returns the path to the given LevelPack.*/
	private TreePath getPath(LevelPack pack) {
		DefaultMutableTreeNode root = getRootNode();
		TreePath path = new TreePath(root);
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
			PackInfo pi = (PackInfo) child.getUserObject();
			if (pi.pack.equals(pack)) {
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
		return ((PackInfo) packNode.getUserObject()).pack;
	}


	/**Returns the complete, non-partial, selected LevelPack.  The LevelPack's current world will be set appropriately.*/
	public LevelPack getSelectedLevelPack() {
		throw new RuntimeException("Not implemented yet.");
	}


	// ===============================================================
	// ========== LevelPackScreen EDITING STUFF ======================
	// ===============================================================

	private Undoable<Image> changePackImage() {
		File file = FileControl.openDialog(LevelPackScreen.this);
		Image img = UIBuilder.getImage(file.getPath(), true);
		if (img == null) {
			JOptionPane.showMessageDialog(LevelPackScreen.this, "Cannot load the given image:" + file.getPath());
			return null;
		}
		PackInfo pInfo = (PackInfo) getCurrentSelection();

		pInfo.hasChanged = true;

		Image oldImg = pInfo.pack.getEmblem();
		Image newImg = img.getScaledInstance(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT, Image.SCALE_SMOOTH);
		pInfo.pack.setEmblem(newImg);
		refreshInfoDisplay(_InfoPnl);
		_Tree.repaint();
		return new Undoable<Image>(oldImg, newImg, pInfo) {

			@Override
			protected boolean validateBeforeUndo() {
				PackInfo info = (PackInfo) context;
				return (info.pack.getEmblem().equals(after));
			}


			@Override
			protected boolean validateBeforeRedo() {
				PackInfo info = (PackInfo) context;
				return (info.pack.getEmblem().equals(before));
			}


			@Override
			protected void undoValidated() {
				PackInfo info = (PackInfo) context;
				info.pack.setEmblem(before);
			}


			@Override
			protected void redoValidated() {
				PackInfo info = (PackInfo) context;
				info.pack.setEmblem(after);
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
		wInfo.hasChanged = true;
		Image oldImg = wInfo.emblem;
		Image newImg = (wInfo.emblem = img.getScaledInstance(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT,
				Image.SCALE_SMOOTH));
		refreshInfoDisplay(_InfoPnl);
		_Tree.repaint();
		return new Undoable<Image>(oldImg, newImg, wInfo) {

			@Override
			protected boolean validateBeforeUndo() {
				WorldInfo info = (WorldInfo) context;
				return (info.emblem.equals(after));
			}


			@Override
			protected boolean validateBeforeRedo() {
				WorldInfo info = (WorldInfo) context;
				return (info.emblem.equals(before));
			}


			@Override
			protected void undoValidated() {
				WorldInfo info = (WorldInfo) context;
				info.emblem = before;
			}


			@Override
			protected void redoValidated() {
				WorldInfo info = (WorldInfo) context;
				info.emblem = after;
			}
		};
	}


	// ===============================================================
	// ========== LevelPackScreen HELPER CLASSES =====================
	// ===============================================================


	/**A data structure that associates a LevelPack with its original JSON String.  This is useful because the tree list 
	 * cannot fully deserialize every LevelPack, it would take too long.  */
	private static final class PackInfo {

		public final LevelPack pack;
		public final String originalJson;
		public boolean hasChanged = false;


		public PackInfo(LevelPack pack, String json) {
			this.pack = pack;
			this.originalJson = json;
		}
	}


	/**A data structure that embodies the "partial" deserialization of a World/level, and associates it with its original 
	 * LevelPack and index.  This is useful because the tree list cannot fully deserialize every World in every 
	 * LevelPack, it would just take too long.*/
	private static final class WorldInfo {

		public String title;
		public String description;
		public Image emblem;
		public final LevelPack levelPack;
		public final int originalIndex;
		public boolean hasChanged = false;


		private WorldInfo(LevelPack levelPack, String title, String description, Image emblem, int index) {
			this.levelPack = levelPack;
			this.title = title;
			this.description = description;
			this.emblem = emblem;
			this.originalIndex = index;
		}


		/**Reads only enough of the level's basic info to produce a WorldInfo.*/
		public static WorldInfo fromLevel(LevelPack pack, int index) {
			String title = pack.getLevelTitle(index);
			String desc = pack.getLevelDescription(index);
			Image img = pack.getLevelEmblem(index);
			WorldInfo wi = new WorldInfo(pack, title, desc, img, index);
			return wi;
		}


		public static WorldInfo fromNew() {
			throw new RuntimeException("Not implemented yet.");
		}
	}


	private static final TreeCellRenderer _TreeRenderer = new TreeCellRenderer() {

		private final EmptyBorder spacer = new EmptyBorder(2, 2, 2, 2);


		@Override
		public final Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
				boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
			if (value instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				if (node.getUserObject() instanceof PackInfo) {
					PackInfo pi = (PackInfo) node.getUserObject();
					return getLevelPackComponent(pi.pack, isSelected, isExpanded, isLeaf, row, hasFocus);
				} else if (node.getUserObject() instanceof WorldInfo) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
					int idx = parent.getIndex(node);
					LevelPack pack = ((PackInfo) parent.getUserObject()).pack;
					return getWorldInfoComponent(pack, (WorldInfo) node.getUserObject(), isSelected, isExpanded, isLeaf,
							idx, hasFocus);
				} else if (node.getUserObject() instanceof String) {
					return new JLabel(node.getUserObject().toString());
				}

			}
			throw new RuntimeException("Sanity check.  This shouldn't happen.");
		}


		/**Returns a visual component for displaying a LevelPack within the tree.*/
		private final Component getLevelPackComponent(LevelPack pack, boolean isSelected, boolean isExpanded,
				boolean isLeaf, int row, boolean hasFocus) {
			JPanel pnl = new JPanel();
			pnl.setOpaque(false);
			pnl.setLayout(new HorizontalLayout());
			pnl.setBorder(new EmptyBorder(2, 2, 2, 2));
			pnl.add(UIBuilder.buildLabel().image(pack.getEmblem().getScaledInstance(50, 50, Image.SCALE_FAST))
					.border(new EmptyBorder(2, 2, 2, 2)).create());
			pnl.add(UIBuilder.buildLabel().text(pack.getName() + " - ").border(spacer).create());
			pnl.add(UIBuilder.buildLabel().text(pack.getDescription()).border(new EmptyBorder(2, 2, 2, 2)).create());
			String author = pack.getOriginalAuthor() == null ? "unknown author"
					: pack.getOriginalAuthor().getUserName();
			pnl.add(UIBuilder.buildLabel().text("by " + author).border(new EmptyBorder(2, 2, 2, 2)).create());
			return pnl;

		}


		/**Returns a visual component for displaying a World within the tree.*/
		private final Component getWorldInfoComponent(LevelPack pack, WorldInfo info, boolean isSelected,
				boolean isExpanded, boolean isLeaf, int row, boolean hasFocus) {
			JPanel pnl = new JPanel();
			pnl.setOpaque(false);
			pnl.setLayout(new HorizontalLayout());
			pnl.setBorder(new EmptyBorder(2, 2, 2, 2));
			pnl.add(UIBuilder.buildLabel().image(info.emblem.getScaledInstance(50, 50, Image.SCALE_FAST))
					.border(new EmptyBorder(2, 2, 2, 2)).create());
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
			case "CHANGE_LEVELPACK_EMBLEM":
				Undoable<Image> packEmblemChange = changePackImage();
				if (packEmblemChange != null)
					_UndoStack.push(packEmblemChange);
				return;
			case "SAVE_PACK":
			default:
				System.out.println(this.getClass().getName() + " has not implemented command: " + e.getActionCommand());
			}

		}


		/**Called when the tree's selection changes.*/
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			refreshInfoDisplay(_InfoPnl);
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
