package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
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
import com.undead_pixels.dungeon_bots.ui.JPackDownloadDialog;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.undo.UndoStack;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;

/**
 * The screen where a user selects what level/pack they want to play
 */
public class LevelPackScreen extends Screen {

	private static final String FIELD_LEVEL_TITLE = "Level title";
	private static final String FIELD_LEVEL_DESCRIPTION = "Level description";
	private static final String FIELD_PACK_TITLE = "Pack title";
	private static final String FIELD_PACK_DESCRIPTION = "Pack description";
	private static final String FIELD_PUBLISH_START = "Publication start";
	private static final String FIELD_PUBLISH_END = "Publication end";

	public static final int THUMBNAIL_WIDTH = 75;
	public static final int THUMBNAIL_HEIGHT = 50;
	private static final long serialVersionUID = 1L;
	private JTree _Tree = null;
	private JPanel _InfoPnl = null;

	private JButton _BttnPlayLevel;
	private JButton _BttnEditLevel;
	private JButton _BttnAddWorld;
	private JButton _BttnRemoveItem;
	private JButton _BttnWorldUp;
	private JButton _BttnWorldDown;
	private JButton _BttnLockPack;
	private JButton _BttnUndo;
	private JButton _BttnRedo;
	private JButton _BttnEditScript;
	private JButton _BttnSave;

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
		ArrayList<String> filenames = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			String absPath = files[i].getPath();
			String json = Serializer.readStringFromFile(absPath);
			if (json == null)
				System.err.println("Could not read file: " + files[i].getPath());
			else {
				jsons.add(json);
				filenames.add(absPath);
			}
		}
		LevelPackScreen lps = fromJSONs(jsons.toArray(new String[jsons.size()]),
				filenames.toArray(new String[filenames.size()]));
		return lps;
	}


	/**Returns a LevelPackScreen whose packs are built (partially) from the given JSON strings.*/
	public static LevelPackScreen fromJSONs(String[] jsons) {
		String[] filenames = new String[jsons.length];
		for (int i = 0; i < filenames.length; i++)
			filenames[i] = "";
		return fromJSONs(jsons, filenames);
	}


	/**Returns a LevelPackScreen whose packs are built (partially) from the given JSON strings, and 
	 * associated with the given filenames.*/
	private static LevelPackScreen fromJSONs(String[] jsons, String[] filenames) {
		// Figure out which packs come from good JSONs.
		ArrayList<LevelPack> goodPacks = new ArrayList<LevelPack>();
		ArrayList<String> goodJsons = new ArrayList<String>();
		ArrayList<String> goodFilenames = new ArrayList<String>();
		for (int i = 0; i < jsons.length; i++) {
			LevelPack lp = LevelPack.fromJsonPartial(jsons[i]);
			if (lp == null) {
				System.err.println(filenames[i] + " unrecognized LevelPack format for partial read.");
				continue;
			}
			goodPacks.add(lp);
			goodJsons.add(jsons[i]);
			goodFilenames.add(filenames[i]);
		}

		// Build the screens.
		PackInfo[] pInfos = new PackInfo[goodPacks.size()];
		for (int i = 0; i < goodPacks.size(); i++) {
			pInfos[i] = PackInfo.withJSON(goodPacks.get(i), goodJsons.get(i));
			pInfos[i].filename = goodFilenames.get(i);
		}
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
		packInfoBttns.add(UIBuilder.buildButton().image("icons/load.png").text("Download").mnemonic('d')
				.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).toolTip("Load a Pack from disk.")
				.action("DOWNLOAD_LEVELPACK", getController()).focusable(false).create());
		packInfoBttns.add(UIBuilder.buildButton().image("icons/new.png").text("New pack")
				.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).toolTip("Create a new Pack.")
				.action("ADD_NEW_PACK", getController()).focusable(false).create());
		packInfoBttns.add(_BttnSave = UIBuilder.buildButton().image("icons/save.png").text("Save pack")
				.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).toolTip("Save this LevelPack.")
				.action("SAVE_LEVELPACK", getController()).focusable(false).create());
		packInfoBttns.add(_BttnUndo = UIBuilder.buildButton().image("icons/undo.png").text("Undo")
				.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).toolTip("Undo last change.")
				.action("UNDO", getController()).focusable(false).enabled(false).create());
		packInfoBttns.add(_BttnRedo = UIBuilder.buildButton().image("icons/redo.png").text("Redo")
				.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).toolTip("Redo last change.")
				.action("REDO", getController()).focusable(false).enabled(false).create());
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
		treeBttns.add(_BttnLockPack = UIBuilder.buildButton().image("icons/lock.png").text("Lock")
				.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).toolTip("Lock this LevelPack.")
				.action("LOCK_LEVELPACK", getController()).focusable(false).create());
		// treeBttns.add(_BttnEditScript =
		// UIBuilder.buildButton().image("icons/text preview.png")
		// .toolTip("Edit the transition
		// script.").action("EDIT_TRANSITION_SCRIPT", getController())
		// .focusable(false).create());
		treeBttns.add(
				_BttnEditLevel = UIBuilder.buildButton().image("icons/modify.png").action("EDIT_WORLD", getController())
						.text("Editor").textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).focusable(false)
						.toolTip("Open this world in the editor.").create());
		treeBttns.add(_BttnRemoveItem = UIBuilder.buildButton().image("icons/erase.png").text("Delete")
				.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).action("REMOVE_ITEM", getController())
				.focusable(false).toolTip("Remove this world or pack.").create());
		treeBttns.add(
				_BttnAddWorld = UIBuilder.buildButton().image("icons/add.png").action("ADD_NEW_WORLD", getController())
						.text("Add world").textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).focusable(false)
						.toolTip("Add a new world to this pack.").create());
		treeBttns.add(_BttnWorldUp = UIBuilder.buildButton().image("icons/up.png").action("WORLD_UP", getController())
				.text("Move up").textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).focusable(false)
				.toolTip("Move this world up one slot.").create());
		treeBttns.add(
				_BttnWorldDown = UIBuilder.buildButton().image("icons/down.png").action("WORLD_DOWN", getController())
						.text("Move down").textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).focusable(false)
						.toolTip("Move this world down one slot.").create());
		treeBttns.add(_BttnPlayLevel = UIBuilder.buildButton().image("icons/play.png").text("PLAY")
				.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM).toolTip("Play this world.")
				.action("PLAY_LEVEL", getController()).focusable(true).create());

		treeBttns.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel worldPanel = new JPanel(new BorderLayout());
		worldPanel.add(new JScrollPane(_Tree), BorderLayout.CENTER);
		worldPanel.add(treeBttns, BorderLayout.PAGE_END);


		this.setLayout(new BorderLayout());
		this.add(packPnl, BorderLayout.LINE_START);
		this.add(worldPanel, BorderLayout.CENTER);
		updateGUI();
	}


	private void updateUndoRedo() {
		_BttnUndo.setEnabled(_UndoStack.peekUndo() != null);
		_BttnRedo.setEnabled(_UndoStack.peekRedo() != null);

	}


	void updateGUI() {
		refreshInfoDisplay(_InfoPnl);
		Object selection = getCurrentSelection();
		updateUndoRedo();
		if (selection == null) {
			_BttnPlayLevel.setEnabled(false);
			_BttnEditLevel.setEnabled(false);
			_BttnWorldUp.setEnabled(false);
			_BttnWorldDown.setEnabled(false);
			_BttnAddWorld.setEnabled(false);
			_BttnRemoveItem.setEnabled(false);
			_BttnLockPack.setEnabled(false);
			_BttnSave.setEnabled(false);
			// _BttnEditScript.setEnabled(false);
		} else if (selection instanceof WorldInfo) {
			WorldInfo wInfo = (WorldInfo) selection;
			boolean hasAuthorPermission = wInfo.packInfo.hasAuthorPermission();
			_BttnPlayLevel.setEnabled(true);
			_BttnEditLevel.setEnabled(wInfo.packInfo.hasAuthorPermission());
			_BttnWorldUp.setEnabled(wInfo.packInfo.worlds.indexOf(wInfo) > 0 && hasAuthorPermission);
			_BttnWorldDown.setEnabled(
					wInfo.packInfo.worlds.indexOf(wInfo) < wInfo.packInfo.worlds.size() - 1 && hasAuthorPermission);
			_BttnAddWorld.setEnabled(hasAuthorPermission);
			_BttnRemoveItem.setEnabled(hasAuthorPermission);
			_BttnLockPack.setEnabled(false);
			_BttnSave.setEnabled(hasAuthorPermission);
			// _BttnEditScript.setEnabled(false);
		} else if (selection instanceof PackInfo) {
			PackInfo selPack = (PackInfo) selection;
			_BttnPlayLevel.setEnabled(false);
			_BttnEditLevel.setEnabled(false);
			_BttnWorldUp.setEnabled(false);
			_BttnWorldDown.setEnabled(false);
			_BttnAddWorld.setEnabled(selPack.hasAuthorPermission());
			_BttnRemoveItem.setEnabled(selPack.hasAuthorPermission());
			_BttnSave.setEnabled(selPack.hasAuthorPermission());
			// _BttnEditScript.setEnabled(selPack.hasAuthorPermission());

			if (!selPack.hasAuthorPermission())
				_BttnLockPack.setEnabled(false);
			else {
				_BttnLockPack.setEnabled(true);
				if (selPack.getPack().getLocked())
					_BttnLockPack.setIcon(new ImageIcon("icons/unlock.png"));
				else
					_BttnLockPack.setIcon(new ImageIcon("icons/lock.png"));
			}
		} else
			assert (false); // sanity check
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
			boolean asAuthor = info.packInfo.hasAuthorPermission();
			if (asAuthor) {
				JButton bttnEmblem = UIBuilder.buildButton().image(info.getEmblem()).text("Click to change emblem.")
						.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM)
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
			list.add(createDisplayLine(FIELD_LEVEL_TITLE, info.title, asAuthor));
			list.add(createDisplayLine(FIELD_LEVEL_DESCRIPTION, info.description, asAuthor));
		} else if (selection instanceof PackInfo) {
			PackInfo info = (PackInfo) selection;
			boolean asAuthor = info.hasAuthorPermission();
			if (asAuthor) {
				JButton bttnEmblem = UIBuilder.buildButton().image(info.getEmblem()).text("Click to change emblem.")
						.textPosition(SwingConstants.CENTER, SwingConstants.BOTTOM)
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
			list.add(createDisplayLine(FIELD_PACK_TITLE, info.name, asAuthor));
			// Original author can't change.
			list.add(createDisplayLine("Author",
					(info.originalAuthor == null) ? LevelPack.UNKNOWN_AUTHOR_NAME : info.originalAuthor.getUserName(),
					false));
			list.add(createDisplayLine(FIELD_PACK_DESCRIPTION, info.description, asAuthor));
			list.add(Box.createVerticalStrut(10));
			// Original creation date cannot change.
			list.add(createDisplayLine("Created", info.creationDate, false));
			list.add(createDisplayLine(FIELD_PUBLISH_START, info.publishDate, asAuthor));
			list.add(createDisplayLine(FIELD_PUBLISH_END, info.expireDate, asAuthor));
			list.add(Box.createVerticalStrut(10));
			list.add(createDisplayLine("Feedback", info.feedbackModel, asAuthor));
			list.add(Box.createVerticalStrut(10));
			list.add(UIBuilder.buildButton().text("Scripts").action("EDIT_PACK_SCRIPTS", getController()).prefWidth(80)
					.focusable(false).create());
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
				picker.setName(name);
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
			// Don't use an ActionListener, because that fires only on pushing
			// 'enter' in the field. Instead, use a DocumentListener (or a
			// FieldListener, which implements DocumentListener).
			field.getDocument().addDocumentListener(new FieldListener(field));
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

	/**Returns the node that contains this PackInfo.  If there is no such node, returns null.*/
	private DefaultMutableTreeNode getContainingNode(PackInfo pInfo) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) _Tree.getModel().getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode candidate = (DefaultMutableTreeNode) root.getChildAt(i);
			if (candidate.getUserObject().equals(pInfo))
				return candidate;
		}
		return null;
	}


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
		if (pack == null)
			_Tree.clearSelection();
		else {
			TreePath path = getPath(pack);
			_Tree.setSelectionPath(path);
		}
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
			//pInfo.worlds.clear();
			DefaultMutableTreeNode packNode = new DefaultMutableTreeNode(pInfo);
			rootNode.add(packNode);
			for (WorldInfo wInfo : pInfo.worlds){
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
				m.addTreeModelListener((TreeModelListener) getController());
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


	static boolean saveAsPack(PackInfo pInfo) {
		if (pInfo.filename == null)
			pInfo.filename = "unnamed." + LevelPack.EXTENSION;
		File saveLevelPackFile = FileControl.saveAsDialog(null, new File(pInfo.filename).getParent());
		if (saveLevelPackFile == null) {
			System.out.println("Save cancelled.");
			return false;
		} else {
			return save(pInfo, saveLevelPackFile);
		}
	}


	static boolean savePack(PackInfo pInfo) {
		if (pInfo.filename == null || pInfo.filename.equals(""))
			return saveAsPack(pInfo);
		File file = new File(pInfo.filename);
		if (!file.exists()) {
			return saveAsPack(pInfo);
		}
		return save(pInfo, file);
	}


	static boolean save(PackInfo pInfo, File file) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			LevelPack lp = pInfo.writeComplete();
			String json = lp.toJson();
			writer.write(json);
			System.out.println("Save LevelPack complete to " + file.getPath());
			return true;
		} catch (Exception ioex) {
			ioex.printStackTrace();
			return false;
		}
	}


	// ===============================================================
	// ========== LevelPackScreen EDITING STUFF ======================
	// ===============================================================

	private Undoable<DefaultMutableTreeNode> openLevelPack() {
		File f = FileControl.openPackDialog(this);
		if (f == null) {
			System.out.println("LevelPack open cancelled.");
			return null;
		}
		String json = Serializer.readStringFromFile(f.getPath());

		LevelPack lp = LevelPack.fromJson(json);
		PackInfo pInfo = PackInfo.withJSON(lp, json);
		return addNewPack(pInfo);
	}


	private Undoable<DefaultMutableTreeNode> addNewPack() {
		LevelPack newPack = new LevelPack("New pack.", DungeonBotsMain.instance.getUser(), new World());
		PackInfo pInfo = PackInfo.withoutJSON(newPack);
		return addNewPack(pInfo);
	}


	private Undoable<DefaultMutableTreeNode> addNewPack(PackInfo pInfo) {

		DefaultTreeModel model = (DefaultTreeModel) _Tree.getModel();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
		DefaultMutableTreeNode packNode = new DefaultMutableTreeNode(pInfo);
		for (int i = 0; i < pInfo.worlds.size(); i++) {
			WorldInfo wInfo = pInfo.worlds.get(i);
			DefaultMutableTreeNode worldNode = new DefaultMutableTreeNode(wInfo);
			model.insertNodeInto(worldNode, packNode, packNode.getChildCount());
		}

		
		model.insertNodeInto(packNode, rootNode, rootNode.getChildCount());		
		this.setSelection(pInfo.getPack());
		pInfo.hasChanged = true;
		Undoable<DefaultMutableTreeNode> u = new Undoable<DefaultMutableTreeNode>(rootNode, packNode, model) {

			@Override
			protected void undoValidated() {
				if (!after.getParent().equals(before))
					error();
				if (!before.isNodeChild(after))
					error();
				DefaultTreeModel model = (DefaultTreeModel) context;
				model.removeNodeFromParent(after);
			}


			@Override
			protected void redoValidated() {
				if (after.getParent() != null)
					error();
				if (before.isNodeChild(after))
					error();
				DefaultTreeModel model = (DefaultTreeModel) context;
				model.insertNodeInto(after, before, before.getChildCount());
				setSelection(((PackInfo) packNode.getUserObject()).getPack());
			}

		};
		return u;
	}


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
		DefaultTreeModel model = (DefaultTreeModel) _Tree.getModel();
		DefaultMutableTreeNode nodeWorld = new DefaultMutableTreeNode(newWorld);
		model.insertNodeInto(nodeWorld, nodePack, nodePack.getChildCount());

		Undoable<DefaultMutableTreeNode> u = new Undoable<DefaultMutableTreeNode>(nodePack, nodeWorld, model) {


			@Override
			protected void undoValidated() {
				if (!after.getParent().equals(before))
					error();
				DefaultTreeModel model = (DefaultTreeModel) context;
				if (model.getPathToRoot(nodeWorld) == null)
					error();
				model.removeNodeFromParent(after);
				((PackInfo) before.getUserObject()).worlds.remove((WorldInfo) after.getUserObject());

			}


			@Override
			protected void redoValidated() {
				if (after.getParent().equals(before))
					error();
				DefaultTreeModel model = (DefaultTreeModel) context;
				if (model.getPathToRoot(nodeWorld) != null)
					error();
				model.insertNodeInto(after, before, before.getChildCount());
				((PackInfo) before.getUserObject()).worlds.add((WorldInfo) after.getUserObject());
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
		File file = FileControl.openImageDialog(LevelPackScreen.this);
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
			protected void undoValidated() {
				PackInfo info = (PackInfo) context;
				// No error checking, because the image is rescaled which
				// changes the reference to the image.
				// if (!info.getEmblem().equals(after)) error();
				info.setEmblem(before);
				_Tree.repaint();
			}


			@Override
			protected void redoValidated() {
				PackInfo info = (PackInfo) context;
				// No error checking, because the image is rescaled which
				// changes the reference to the image.
				// if (!info.getEmblem().equals(before)) error();
				info.setEmblem(after);
				_Tree.repaint();
			}
		};
	}


	public Undoable<LocalDateTime> changePublishStart(LocalDateTime newLDT) {
		PackInfo pInfo = (PackInfo) getCurrentSelection();
		LocalDateTime oldLDT = pInfo.publishDate;
		pInfo.publishDate = newLDT;
		Undoable<LocalDateTime> u = new Undoable<LocalDateTime>(oldLDT, newLDT, pInfo) {

			@Override
			protected void undoValidated() {
				PackInfo pInfo = (PackInfo) context;
				if (!pInfo.publishDate.equals(after))
					error();
				pInfo.publishDate = before;
			}


			@Override
			protected void redoValidated() {
				PackInfo pInfo = (PackInfo) context;
				if (!pInfo.publishDate.equals(before))
					error();
				pInfo.publishDate = after;
			}

		};
		return u;
	}


	public Undoable<?> changePublishEnd(LocalDateTime newLDT) {
		PackInfo pInfo = (PackInfo) getCurrentSelection();
		LocalDateTime oldLDT = pInfo.expireDate;
		pInfo.expireDate = newLDT;
		Undoable<LocalDateTime> u = new Undoable<LocalDateTime>(oldLDT, newLDT, pInfo) {

			@Override
			protected void undoValidated() {
				PackInfo pInfo = (PackInfo) context;
				if (!pInfo.expireDate.equals(after))
					error();
				pInfo.expireDate = before;
			}


			@Override
			protected void redoValidated() {
				PackInfo pInfo = (PackInfo) context;
				if (!pInfo.expireDate.equals(before))
					error();
				pInfo.expireDate = after;
			}


		};
		return u;
	}


	private Undoable<Image> changeLevelImage() {
		File file = FileControl.openImageDialog(LevelPackScreen.this);
		if (file == null)
			return null;
		Image newImg = UIBuilder.getImage(file.getPath(), true);
		if (newImg == null) {
			JOptionPane.showMessageDialog(LevelPackScreen.this, "Cannot load the given image:" + file.getPath());
			return null;
		}
		WorldInfo wInfo = (WorldInfo) getCurrentSelection();
		wInfo.packInfo.hasChanged = true;
		Image oldImg = wInfo.getEmblem();
		wInfo.setEmblem(newImg);
		refreshInfoDisplay(_InfoPnl);
		_Tree.repaint();
		return new Undoable<Image>(oldImg, wInfo.getEmblem(), wInfo) {

			@Override
			protected void undoValidated() {
				WorldInfo info = (WorldInfo) context;
				// No error checking, because the image is rescaled which
				// changes the reference to the image.
				// if (!info.getEmblem().equals(after)) error();
				info.setEmblem(before);
				_Tree.repaint();
			}


			@Override
			protected void redoValidated() {
				WorldInfo info = (WorldInfo) context;
				// No error checking, because the image is rescaled which
				// changes the reference to the image.
				// if (!info.getEmblem().equals(before)) error();
				info.setEmblem(after);
				_Tree.repaint();
			}
		};
	}


	private Undoable<String> changeLevelDescription(String newDescription) {
		WorldInfo wInfo = (WorldInfo) getCurrentSelection();
		wInfo.packInfo.hasChanged = true;
		String oldDescription = wInfo.description;
		wInfo.description = newDescription;
		Undoable<String> u = new Undoable<String>(oldDescription, newDescription, wInfo) {

			@Override
			protected void undoValidated() {
				WorldInfo info = (WorldInfo) context;
				if (!info.description.equals(after))
					error();
				info.description = before;
			}


			@Override
			protected void redoValidated() {
				WorldInfo info = (WorldInfo) context;
				if (!info.description.equals(before))
					error();
				info.description = after;
			}

		};
		return u;
	}


	private Undoable<String> changeLevelTitle(String newTitle) {
		WorldInfo wInfo = (WorldInfo) getCurrentSelection();
		wInfo.packInfo.hasChanged = true;
		String oldTitle = wInfo.title;
		wInfo.title = newTitle;
		Undoable<String> u = new Undoable<String>(oldTitle, newTitle, wInfo) {

			@Override
			protected void undoValidated() {
				WorldInfo info = (WorldInfo) context;
				if (!info.title.equals(after))
					error();
				info.title = before;
			}


			@Override
			protected void redoValidated() {
				WorldInfo info = (WorldInfo) context;
				if (!info.title.equals(before))
					error();
				info.title = after;
			}

		};
		return u;
	}


	private Undoable<PackInfo> removePack(PackInfo pInfo) {
		DefaultMutableTreeNode packNode = getContainingNode(pInfo);
		DefaultTreeModel model = (DefaultTreeModel) _Tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		model.removeNodeFromParent(packNode);
		if (pInfo.filename != null) {
			File f = new File(pInfo.filename);
			if (f.exists())
				try {
					Files.delete(f.toPath());
				} catch (IOException e) {
					System.err.println("Could not delete file " + pInfo.filename + " - " + e.getMessage());
				}
		}

		this.setSelection(null);
		return new Undoable<PackInfo>(null, pInfo, model) {

			@Override
			protected void undoValidated() {
				if (getContainingNode(pInfo) != null)
					error();
				model.insertNodeInto(packNode, root, root.getChildCount());
			}


			@Override
			protected void redoValidated() {
				if (getContainingNode(pInfo) == null)
					error();
				model.removeNodeFromParent(packNode);
			}

		};
	}


	private Undoable<WorldInfo> removeWorld(WorldInfo wInfo) {
		PackInfo pInfo = wInfo.packInfo;
		Integer idx = pInfo.worlds.indexOf(wInfo);
		if (idx < 0)
			return null;
		DefaultMutableTreeNode packNode = getContainingNode(pInfo);
		if (packNode == null)
			return null;
		DefaultMutableTreeNode worldNode = (DefaultMutableTreeNode) packNode.getChildAt(idx);
		DefaultTreeModel model = (DefaultTreeModel) _Tree.getModel();
		model.removeNodeFromParent(worldNode);
		pInfo.worlds.remove(idx);
		this.setSelection(null);
		return new Undoable<WorldInfo>(null, wInfo) {

			@Override
			protected void undoValidated() {
				if (pInfo.worlds.contains(wInfo))
					error();
				model.insertNodeInto(worldNode, packNode, idx);
				pInfo.worlds.add(idx, wInfo);
			}


			@Override
			protected void redoValidated() {
				if (!pInfo.worlds.contains(wInfo))
					error();
				model.removeNodeFromParent(packNode);
				pInfo.worlds.remove(idx);
			}

		};

	}


	/**Toggles the lock status of the indicated LevelPack (if the current user has authority or is not barred from 
	 * changing the lock status.  Returns the resulting lock status.*/
	private Undoable<Boolean> toggleLock(PackInfo selPack) {
		LevelPack pack = selPack.getPack();
		if (!pack.isAuthor(DungeonBotsMain.instance.getUser()) && pack.getAllAuthors().length != 0) {
			JOptionPane.showMessageDialog(LevelPackScreen.this,
					"You cannot lock or unlock this level pack because you are not an author.");
			return null;
		}
		boolean oldLocked = pack.getLocked();
		pack.setLocked(!oldLocked);
		if (pack.getLocked())
			_BttnLockPack.setIcon(new ImageIcon((BufferedImage) UIBuilder.getImage("icons/unlock.png")));
		else
			_BttnLockPack.setIcon(new ImageIcon((BufferedImage) UIBuilder.getImage("icons/lock.png")));
		selPack.hasChanged = true;
		Undoable<Boolean> u = new Undoable<Boolean>(oldLocked, !oldLocked, pack) {

			@Override
			protected void undoValidated() {
				LevelPack pack = (LevelPack) context;
				if (pack.getLocked() == before)
					error();
				pack.setLocked(!pack.getLocked());
			}


			@Override
			protected void redoValidated() {
				LevelPack pack = (LevelPack) context;
				if (pack.getLocked() == after)
					error();
				pack.setLocked(!pack.getLocked());
			}

		};
		return u;
	}


	private Undoable<Integer> moveWorldDown(WorldInfo wInfo) {
		PackInfo pInfo = wInfo.packInfo;
		DefaultMutableTreeNode packNode = getContainingNode(pInfo);
		if (packNode == null)
			return null;
		int oldIndex = pInfo.worlds.indexOf(wInfo);
		if (oldIndex < 0)
			return null;
		int newIndex = oldIndex + 1;
		DefaultMutableTreeNode worldNode = (DefaultMutableTreeNode) packNode.getChildAt(oldIndex);
		DefaultTreeModel model = (DefaultTreeModel) _Tree.getModel();
		model.removeNodeFromParent(worldNode);
		model.insertNodeInto(worldNode, packNode, newIndex);
		pInfo.worlds.remove(oldIndex);
		pInfo.worlds.add(newIndex, wInfo);
		pInfo.hasChanged = true;
		this.setSelection(pInfo.getPack(), newIndex);
		return new RePositionedWorldUndoable(oldIndex, newIndex, packNode);
	}


	private Undoable<Integer> moveWorldUp(WorldInfo wInfo) {
		PackInfo pInfo = wInfo.packInfo;
		DefaultMutableTreeNode packNode = getContainingNode(pInfo);
		if (packNode == null)
			return null;
		int oldIndex = pInfo.worlds.indexOf(wInfo);
		if (oldIndex <= 0)
			return null;
		int newIndex = oldIndex - 1;
		DefaultMutableTreeNode worldNode = (DefaultMutableTreeNode) packNode.getChildAt(oldIndex);
		DefaultTreeModel model = (DefaultTreeModel) _Tree.getModel();
		model.removeNodeFromParent(worldNode);
		model.insertNodeInto(worldNode, packNode, newIndex);
		pInfo.worlds.remove(oldIndex);
		pInfo.worlds.add(newIndex, wInfo);
		pInfo.hasChanged = true;
		this.setSelection(pInfo.getPack(), newIndex);
		return new RePositionedWorldUndoable(oldIndex, newIndex, packNode);
	}


	private class RePositionedWorldUndoable extends Undoable<Integer> {

		private final DefaultMutableTreeNode packNode, worldNode;
		private final PackInfo pInfo;
		private final WorldInfo wInfo;


		public RePositionedWorldUndoable(Integer before, Integer after, Object context) {
			super(before, after, context);
			packNode = (DefaultMutableTreeNode) context;
			worldNode = (DefaultMutableTreeNode) packNode.getChildAt(after);
			pInfo = (PackInfo) packNode.getUserObject();
			wInfo = (WorldInfo) worldNode.getUserObject();
		}


		@Override
		protected void undoValidated() {
			if (packNode.getChildCount() <= after)
				error();
			if (packNode.getChildCount() <= before)
				error();
			if (!packNode.getChildAt(after).equals(worldNode))
				error();
			pInfo.worlds.remove(after);
			pInfo.worlds.add(before, wInfo);
			DefaultTreeModel model = (DefaultTreeModel) _Tree.getModel();
			model.removeNodeFromParent(worldNode);
			model.insertNodeInto(worldNode, packNode, before);
		}


		@Override
		protected void redoValidated() {
			if (packNode.getChildCount() <= after)
				error();
			if (packNode.getChildCount() <= before)
				error();
			if (!packNode.getChildAt(before).equals(worldNode))
				error();
			pInfo.worlds.remove(before);
			pInfo.worlds.add(after, wInfo);
			DefaultTreeModel model = (DefaultTreeModel) _Tree.getModel();
			model.removeNodeFromParent(worldNode);
			model.insertNodeInto(worldNode, packNode, after);
		}

	}


	// ===============================================================
	// ========== LevelPackScreen HELPER CLASSES =====================
	// ===============================================================


	/**A data structure that associates a LevelPack with its original JSON String.  This is useful because the tree list 
	 * cannot fully deserialize every LevelPack, it would take too long.  */
	static final class PackInfo {

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

		public String filename;


		/**Returns a new PackInfo, associated with the given JSON string.*/
		public static PackInfo withJSON(LevelPack pack, String json) {
			return new PackInfo(pack, json);
		}


		/**Returns a new PackInfo that does not have an associated JSON string.*/
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
			
			for (int i = 0; i < pack.getLevelCount(); i++){
				WorldInfo wInfo = WorldInfo.fromLevelIndex(this, i);
				worlds.add(wInfo);
				
			}			
		}


		/**Returns true if one of the following is true:  1) the logged in user is a pack author; 2) the pack has not 
		 * authors; or 3) the pack is not locked.  Otherwise, returns false.*/
		public boolean hasAuthorPermission() {
			if (_pack.isAuthor(DungeonBotsMain.instance.getUser()))
				return true;
			if (_pack.getAllAuthors().length == 0)
				return true;
			if (!_pack.getLocked())
				return true;
			return false;
		}


		/**Returns the LevelPack this PackInfo purports to represent.*/
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
		public LevelPack writeComplete() {

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
				BufferedImage[] emblems = new BufferedImage[this.worlds.size()];
				String[] titles = new String[this.worlds.size()];
				String[] descriptions = new String[this.worlds.size()];
				for (int i = 0; i < this.worlds.size(); i++) {
					WorldInfo wInfo = this.worlds.get(i);
					if (wInfo.originalIndex < 0)
						completeWorlds[i] = new World();
					else
						completeWorlds[i] = pack.getWorld(wInfo.originalIndex);

					completeWorlds[i].setName(wInfo.title);
					emblems[i] = createBufferedEmblem(wInfo.getEmblem());
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

				World[] completeWorlds = new World[this.worlds.size()];
				BufferedImage[] emblems = new BufferedImage[this.worlds.size()];
				String[] titles = new String[this.worlds.size()];
				String[] descriptions = new String[this.worlds.size()];
				for (int i = 0; i < this.worlds.size(); i++) {
					WorldInfo wInfo = this.worlds.get(i);
					completeWorlds[i] = new World();
					// completeWorlds[i] = new World(new
					// File(LevelPack.DEFAULT_WORLD_FILE));
					emblems[i] = createBufferedEmblem(wInfo.getEmblem());
					titles[i] = wInfo.title;
					descriptions[i] = wInfo.description;
				}
				pack = new LevelPack(this.name, this.originalAuthor, completeWorlds);
				// pack.setWorlds(completeWorlds);
				pack.setLevelEmblems(emblems);
				pack.setLevelTitles(titles);
				pack.setLevelDescriptions(descriptions);
			}


			// Now, write the particulars of the level pack.
			pack.setEmblem(createBufferedEmblem(this.getEmblem()));
			pack.setName(this.name);
			pack.setDescription(this.description);
			pack.setPublicationStart(this.publishDate);
			pack.setPublicationEnd(this.expireDate);
			pack.setFeedbackModel(this.feedbackModel);

			return pack;
		}


		/**A non-BufferedImage cannot be stored in a LevelPack or World, or it causes problems 
		 * for serialization.*/
		private static BufferedImage createBufferedEmblem(Image img) {
			if (img instanceof BufferedImage)
				return (BufferedImage) img;
			BufferedImage buffered = new BufferedImage(LevelPack.EMBLEM_WIDTH, LevelPack.EMBLEM_HEIGHT,
					BufferedImage.TYPE_INT_RGB);
			buffered.getGraphics().drawImage(img, 0, 0, null);
			return buffered;
		}
	}


	/**A data structure that embodies the "partial" deserialization of a World/level, and associates it with its original 
	 * LevelPack and index.  This is useful because the tree list cannot fully deserialize every World in every 
	 * LevelPack, it would just take too long.*/
	static final class WorldInfo {

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


		/**Creates a new WorldInfo, that has no JSON representation.  Such a WorldInfo will have an originalIndex 
		 * of -1.*/
		public static WorldInfo fromNew(PackInfo info, String title, String description, Image emblem) {
			return new WorldInfo(info, title, description, emblem, -1);
		}


	}


	/**Marshals undoable changes to a JTextField's Document object.*/
	private final class FieldListener implements DocumentListener {

		private final JTextField field;


		public FieldListener(JTextField field) {
			this.field = field;
		}


		@Override
		public void changedUpdate(DocumentEvent e) {
			handleFieldChange(field, e);
		}


		@Override
		public void insertUpdate(DocumentEvent e) {
			handleFieldChange(field, e);
		}


		@Override
		public void removeUpdate(DocumentEvent e) {
			handleFieldChange(field, e);
		}


		/**Handles a changed field in the GUI, applying the change to the appropriate WorldInfo or PackInfo.*/
		private final void handleFieldChange(JTextField field, DocumentEvent e) {

			Object context = getCurrentSelection();
			Undoable<?> u = null;
			if (context instanceof WorldInfo) {
				switch (field.getName()) {
				case FIELD_LEVEL_TITLE:
					u = changeLevelTitle(field.getText());
					break;
				case FIELD_LEVEL_DESCRIPTION:
					u = changeLevelDescription(field.getText());
					break;
				}
			} else if (context instanceof PackInfo) {
				switch (field.getName()) {
				case FIELD_PACK_TITLE:
					u = changePackName(field.getText());
					break;
				case FIELD_PACK_DESCRIPTION:
					u = changePackDescription(field.getText());
					break;
				}
			}
			if (u != null) {
				_UndoStack.push(u);
				updateUndoRedo();
				return;
			}

			System.out.println("Have not implemented LevelPackScreen.Controller.handleFieldChange() for field '"
					+ field.getName() + "' in context " + context.toString());

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
			if (!pInfo.hasAuthorPermission())
				pnl.add(UIBuilder.buildLabel()
						.image(UIBuilder.getImage("icons/lock.png").getScaledInstance(20, 20, Image.SCALE_SMOOTH))
						.border(new EmptyBorder(5, 5, 5, 5)).create());
			pnl.add(UIBuilder.buildLabel().text(pInfo.name + " - ").border(spacer).create());
			pnl.add(UIBuilder.buildLabel().text(pInfo.description).border(new EmptyBorder(2, 2, 2, 2)).create());
			// String author = pInfo.originalAuthor == null ?
			// LevelPack.UNKNOWN_AUTHOR_NAME :
			// pInfo.originalAuthor.getUserName();
			// pnl.add(UIBuilder.buildLabel().text("by " + author).border(new
			// EmptyBorder(2, 2, 2, 2)).create());
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
			Object sel;
			WorldInfo selWorld;
			PackInfo selPack;
			Undoable<?> u = null;
			switch (e.getActionCommand()) {
			case "ADD_NEW_PACK":
				u = addNewPack();
				break;
			case "ADD_NEW_WORLD":
				u = addNewWorld();
				break;
			case "CHANGE_LEVEL_EMBLEM":
				u = changeLevelImage();
				break;
			case "CHANGE_PACK_EMBLEM":
				u = changePackImage();
				break;
			case "CHANGE_FEEDBACK_MODEL":
				JRadioButton source = (JRadioButton) e.getSource();
				LevelPack.FeedbackModel model = LevelPack.FeedbackModel.valueOf(source.getName());
				u = changeFeedbackModel(model);
				break;
			case "DOWNLOAD_LEVELPACK":
				JPackDownloadDialog jpdd = new JPackDownloadDialog(LevelPackScreen.this);
				jpdd.setVisible(true);
				if (jpdd.getResultPack() == null || jpdd.getResultJson() == null)
					return;
				PackInfo p = PackInfo.withJSON(jpdd.getResultPack(), jpdd.getResultJson());
				u = LevelPackScreen.this.addNewPack(p);
				break;
			case "EDIT_WORLD":
				selWorld = (WorldInfo) getCurrentSelection();
				int idx = selWorld.packInfo.worlds.indexOf(selWorld);
				if (!selWorld.packInfo.hasAuthorPermission()) {
					JOptionPane.showMessageDialog(LevelPackScreen.this,
							"Sorry, you do not have permission to edit this world.");
					return;
				}
				LevelPack completePack = selWorld.packInfo.writeComplete();
				completePack.setCurrentWorld(idx);
				DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen(completePack));
				return;

			case "PLAY_LEVEL":
				selWorld = (WorldInfo) getCurrentSelection();
				int index = selWorld.packInfo.worlds.indexOf(selWorld);
				LevelPack partialPack = selWorld.packInfo.getPack();
				if (!partialPack.isPlayerVisible(index) && !selWorld.packInfo.hasAuthorPermission()) {
					JOptionPane.showMessageDialog(LevelPackScreen.this, "Sorry, this level is currently unavailable.");
					return;
				}
				LevelPack newPack = selWorld.packInfo.writeComplete();
				newPack.setCurrentWorld(index);
				DungeonBotsMain.instance.setCurrentScreen(new GameplayScreen(newPack, false));
				return;
			case "REMOVE_ITEM":
				sel = getCurrentSelection();
				if (sel == null)
					return;
				if (sel instanceof WorldInfo) {
					int result = JOptionPane.showConfirmDialog(LevelPackScreen.this,
							"Are you sure?  Removing this world means it will be unavailable in the future.");
					if (result != JOptionPane.YES_OPTION)
						return;
					u = removeWorld((WorldInfo) sel);
				} else if (sel instanceof PackInfo) {
					int result = JOptionPane.showConfirmDialog(LevelPackScreen.this,
							"Are you sure?  Removing this pack will delete its associated file.");
					if (result != JOptionPane.YES_OPTION)
						return;
					u = removePack((PackInfo) sel);
				} else
					throw new RuntimeException("Sanity check.");
				break;
			case "SAVE_LEVELPACK":
				sel = getCurrentSelection();
				if (sel instanceof PackInfo) {
					PackInfo pInfo = (PackInfo) sel;
					savePack(pInfo);
				} else if (sel instanceof WorldInfo) {
					PackInfo pInfo = ((WorldInfo) sel).packInfo;
					savePack(pInfo);
				} else
					throw new RuntimeException("Sanity check.");
				return;
			case "LOCK_LEVELPACK":
				selPack = (PackInfo) getCurrentSelection();
				u = toggleLock(selPack);
				break;
			case "WORLD_DOWN":
				u = moveWorldDown((WorldInfo) getCurrentSelection());
				break;
			case "WORLD_UP":
				u = moveWorldUp((WorldInfo) getCurrentSelection());
				break;
			case "OPEN_LEVELPACK_FILE":
				u = openLevelPack();
				break;
			case "UNDO":
				if (_UndoStack.peekUndo() != null) {
					_UndoStack.popUndo().undo();
					updateGUI();
				}
				break;
			case "REDO":
				if (_UndoStack.peekRedo() != null) {
					_UndoStack.popRedo().redo();
					updateGUI();
				}
				break;
			case JXDatePicker.COMMIT_KEY:
				JXDatePicker picker = (JXDatePicker) e.getSource();
				java.util.Date d = picker.getDate();
				LocalDateTime ldt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
				if (picker.getName().equals(FIELD_PUBLISH_START))
					u = changePublishStart(ldt);
				else if (picker.getName().equals(FIELD_PUBLISH_END))
					u = changePublishEnd(ldt);
				else
					assert (false);
				break;
			case "EDIT_TRANSITION_SCRIPT":

			case "SAVE_ALL":


			default:
				System.out.println(this.getClass().getName() + " has not implemented command: " + e.getActionCommand());
			}

			if (u != null) {
				_UndoStack.push(u);
				updateUndoRedo();
			}
		}


		/**Called when the tree's selection changes.*/
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			updateGUI();

		}


		@Override
		public void treeNodesChanged(TreeModelEvent e) {
			_Tree.revalidate();
			_Tree.repaint();
		}


		@Override
		public void treeNodesInserted(TreeModelEvent e) {
			_Tree.revalidate();
			_Tree.repaint();
		}


		@Override
		public void treeNodesRemoved(TreeModelEvent e) {
			_Tree.revalidate();
			_Tree.repaint();

		}


		@Override
		public void treeStructureChanged(TreeModelEvent e) {
			_Tree.revalidate();
			_Tree.repaint();

		}


	}


}
