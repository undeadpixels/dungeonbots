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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;

/**
 * The screen where a user selects what level/pack they want to play
 */
public class LevelPackScreen extends Screen {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTree _Tree = null;
	private JPanel _DisplayPnl = null;

	/**This is essentially the data model.*/
	private final ArrayList<PackInfo> _Packs = new ArrayList<PackInfo>();
	private final ArrayList<ArrayList<WorldInfo>> _Levels = new ArrayList<ArrayList<WorldInfo>>();


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


	@Override
	protected void addComponents(Container pane) {

		// TODO: an author should be able to edit everything except the
		// OriginalAuthor.
		_DisplayPnl = new JPanel();
		_DisplayPnl.setLayout(new BoxLayout(_DisplayPnl, BoxLayout.Y_AXIS));
		for (Component c : createPackDisplay(DungeonBotsMain.instance.getUser(), null))
			_DisplayPnl.add(c);


		JPanel packInfoBttns = new JPanel();
		packInfoBttns.add(
				UIBuilder.buildButton().text("Open").action("OPEN_PACK", getController()).focusable(false).create());
		packInfoBttns
				.add(UIBuilder.buildButton().text("New").action("NEW_PACK", getController()).focusable(false).create());
		packInfoBttns.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Layout the left-side LevelPack info stuff.
		JPanel packPnl = new JPanel(new BorderLayout());
		packPnl.add(_DisplayPnl, BorderLayout.CENTER);
		packPnl.add(packInfoBttns, BorderLayout.PAGE_END);
		packPnl.setBorder(new EmptyBorder(10, 10, 10, 10));


		// Layout the world tree list stuff on the right.
		DefaultTreeModel dtm = new DefaultTreeModel(createNodes());
		dtm.addTreeModelListener((TreeModelListener) getController());
		_Tree = new JTree(dtm);
		_Tree.setRootVisible(false);
		_Tree.setCellRenderer(_TreeRenderer);
		_Tree.setEditable(true);
		_Tree.setBorder(new EmptyBorder(10, 10, 10, 10));
		_Tree.setExpandsSelectedPaths(true);

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
		// worldPanel.setPreferredSize(new Dimension(300, -1));


		this.setLayout(new BorderLayout());
		this.add(packPnl, BorderLayout.LINE_START);
		this.add(worldPanel, BorderLayout.CENTER);
	}


	/**Create the LevelPack info panel.*/
	private static ArrayList<Component> createPackDisplay(User user, LevelPack pack) {
		ArrayList<Component> pnl = new ArrayList<Component>();
		if (pack != null) {
			JLabel packEmblem = new JLabel(
					new ImageIcon(pack.getEmblem().getScaledInstance(300, 200, Image.SCALE_FAST)));
			packEmblem.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
					new EmptyBorder(10, 10, 10, 10)));
			pnl.add(packEmblem);

			boolean isAuthor = pack.isAuthor(user);
			pnl.add(Box.createVerticalStrut(10));
			pnl.add(packInfo("LevelPack", pack.getName(), isAuthor));
			pnl.add(packInfo("Author", pack.getOriginalAuthor(), isAuthor));
			pnl.add(packInfo("Description", pack.getDescription(), isAuthor));
			pnl.add(Box.createVerticalStrut(10));
			pnl.add(packInfo("Created", pack.getCreationDate(), isAuthor));
			pnl.add(packInfo("Published", pack.getPublishStart(), isAuthor));
			pnl.add(packInfo("Expires", pack.getPublishEnd(), isAuthor));
			pnl.add(Box.createVerticalStrut(10));
			pnl.add(packInfo("Levels", pack.getLevelCount(), isAuthor));
			pnl.add(packInfo("Feedback", pack.getFeedbackModel(), isAuthor));
			pnl.add(Box.createVerticalStrut(10));
		} else {

			JLabel packEmblem = new JLabel(new ImageIcon(
					UIBuilder.getImage("icons/pinion.png").getScaledInstance(300, 200, Image.SCALE_FAST)));
			packEmblem.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
					new EmptyBorder(10, 10, 10, 10)));
			pnl.add(packEmblem);

			pnl.add(Box.createVerticalStrut(10));
			pnl.add(packInfo("LevelPack", "", false));
			pnl.add(packInfo("Author", "", false));
			pnl.add(packInfo("Description", "", false));
			pnl.add(Box.createVerticalStrut(10));
			pnl.add(packInfo("Created", "", false));
			pnl.add(packInfo("Published", "", false));
			pnl.add(packInfo("Expires", "", false));
			pnl.add(Box.createVerticalStrut(10));
			pnl.add(packInfo("Levels", "", false));
			pnl.add(packInfo("Feedback", "", false));
			pnl.add(Box.createVerticalStrut(10));
		}
		return pnl;
	}


	/**Create the tree structure to be displayed, from the given LevelPacks.*/
	private DefaultMutableTreeNode createNodes() {
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


	private static Component packInfo(String name, Object initialContents, boolean isAuthor) {
		if (!isAuthor) {
			JLabel result = new JLabel(name + ": " + initialContents);
			return result;
		}
		JLabel result = new JLabel(name + ": " + initialContents);
		result.setForeground(Color.BLACK);
		return result;
	}


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


		// Update the info displayed on the screen.
		if (_DisplayPnl != null) {
			_DisplayPnl.removeAll();
			for (Component c : createPackDisplay(DungeonBotsMain.instance.getUser(), levelPack))
				_DisplayPnl.add(c);
		}
		if (_Tree != null) {
			// TODO: dynamically update the tree. Putting this off because the
			// tree is built with new nodes to begin with.

		}
	}


	/**Sets the current selection to the given LevelPack.*/
	public void setSelection(LevelPack pack) {
		TreePath path = getPath(pack);		
		_Tree.setSelectionPath(path);
	}


	/**Sets the current selection to the indicated world within the given LevelPack.  Note that if the
	 * world does not exist at the given index, the given LevelPack will be selected.*/
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


	/**Returns the path to the given LevelPack.*/
	private TreePath getPath(LevelPack pack) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) _Tree.getModel().getRoot();
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


	/**Expands all LevelPacks.*/
	public void expandAll() {
		int i = 0;
		while (i < _Tree.getRowCount())
			_Tree.expandRow(i++);
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
		return _Packs.size();
	}


	/**Returns the LevelPack at the given index number.*/
	public LevelPack getLevelPackAt(int index) {
		return _Packs.get(index).pack;
	}


	/**Returns the complete, non-partial, selected LevelPack.  The LevelPack's current world will be set appropriately.*/
	public LevelPack getSelectedLevelPack() {
		LevelPack p;
		throw new RuntimeException("Not implemented yet.");
	}


	private static final class PackInfo {

		public final LevelPack pack;
		public final String json;


		public PackInfo(LevelPack pack, String json) {
			this.pack = pack;
			this.json = json;
		}
	}


	private static final class WorldInfo {

		public final String title;
		public final String description;
		public final Image emblem;
		public final LevelPack levelPack;
		public final int originalIndex;


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
	
	

	private final class Controller extends ScreenController implements TreeModelListener {


		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "OPEN_PACK":

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
