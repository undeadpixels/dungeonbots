/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import com.undead_pixels.dungeon_bots.scene.entities.*;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.EntityType;
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.JWorldEditor;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.WorldView;
import com.undead_pixels.dungeon_bots.ui.undo.Undoable;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

/**
 * The screen for the level editor
 * 
 * @author Wesley
 *
 */
public final class LevelEditorScreen extends Screen {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private WorldView _View;
	public final Tool.SelectionModel selections = new Tool.SelectionModel();

	// Special tools
	private Tool.Selector _Selector;
	private Tool.TilePen _TilePen;
	private Tool.EntityPlacer _EntityPlacer;
	private Tool.ViewControl _ViewControl;


	public LevelEditorScreen(LevelPack levelPack) {
		super(levelPack);
	}

	public LevelEditorScreen() {
		super(new LevelPack("My Level Pack", DungeonBotsMain.instance.getUser(),
				new World()));
	}


	@Override
	protected ScreenController makeController() {
		return new LevelEditorScreen.Controller();
	}


	/** Creates all the entity types available in this Level Editor. */
	public ArrayList<EntityType> createEntityTypes() {
		ArrayList<EntityType> result = new ArrayList<EntityType>();

		// TODO - some of the names produced by lambdas might need to be changed
		// later

		result.add(new EntityType("fish", AssetManager.getTextureRegion("DawnLike/Characters/Aquatic0.png", 2, 1), (x, y) -> {
			// TODO - create new actual entity class
			return new DeletemeEntity(world, AssetManager.getTextureRegion("DawnLike/Characters/Aquatic0.png", 2, 1), x,
					y);
		}));
		result.add(new EntityType("demon", AssetManager.getTextureRegion("DawnLike/Characters/Demon0.png", 2, 3), (x, y) -> {
			// TODO - create new actual entity class
			return new DeletemeEntity(world, AssetManager.getTextureRegion("DawnLike/Characters/Demon0.png", 2, 3), x,
					y);
		}));
		result.add(new EntityType("ghost", AssetManager.getTextureRegion("DawnLike/Characters/Undead0.png", 2, 4), (x, y) -> {
			// TODO - create new actual entity class
			return new DeletemeEntity(world, AssetManager.getTextureRegion("DawnLike/Characters/Undead0.png", 2, 4), x,
					y);
		}));
		result.add(new EntityType("key", AssetManager.getTextureRegion("DawnLike/Items/Key.png", 0, 0), (x, y) -> {
			// TODO - create new actual entity class
			return ItemEntity.key(world, x, y);
		}));
		result.add(new EntityType("chest", ItemChest.LOCKED_TEXTURE, (x, y) -> {
			return new ItemChest(world, "item chest (level editor)", x, y);
		}));
		result.add(new EntityType("door", Door.DEFAULT_TEXTURE, (x, y) -> {
			return new Door(world, x, y);
		}));
		result.add(new EntityType("goal", Goal.DEFAULT_TEXTURE, (x, y) -> {
			return new Goal(world, "goal", x, y);
		}));
		result.add(new EntityType("player", Player.DEFAULT_TEXTURE, (x, y) -> {
			Player ret = new Player(world, "player", x, y);
			return ret;
		}));
		result.add(new EntityType("bot", Bot.DEFAULT_TEXTURE, (x, y) -> {
			return new Bot(world, "bot", x, y);
		}));
		result.add(new EntityType("block", Block.DEFAULT_TEXTURE, (x, y) -> {
			return new Block(world, x, y);
		}));
		result.add(new EntityType("gold", ItemEntity.GOLD_TEXTURE, (x, y) -> {
			return ItemEntity.gold(world, x, y, 2);
		}));
		result.add(new EntityType("gem", ItemEntity.GEM_TEXTURE, (x, y) -> {
			return ItemEntity.gem(world, x, y);
		}));
		result.add(new EntityType("diamond", ItemEntity.DIAMOND_TEXTURE, (x,y) -> {
			return ItemEntity.diamond(world, x, y);
		}));

		return result;
	}


	/**
	 * The Level Editor controller works by maintaining a reference to a Tool to
	 * correctly handle inputs in the game view, whereas the controller itself
	 * handles Level Editor-related actions.
	 */
	protected final class Controller extends ScreenController
			implements ListSelectionListener, MouseWheelListener, MouseInputListener, ChangeListener {

		/** The list managing the selection of a tool. */
		public JList<Tool> toolPalette;
		/** The list managing the selection of a tile type. */
		public JList<TileType> tilePalette;
		/** The list managing the selection of an entity type. */
		public JList<EntityType> entityPalette;

		// ===========================================================
		// ======== LevelEditorScreen.Controller TOOL STUFF
		// ===========================================================

		/**
		 * This stupid boolean is there just to flag a list's clearSelection()
		 * call from in turn clearing other lists.
		 */
		private boolean _PropogateChange = true;


		/** Handle the palette list changes. */
		@Override
		public void valueChanged(ListSelectionEvent e) {

			if (e.getSource() instanceof JList) {

				// The different lists are greedy - choosing a tool, or a
				// tile, or an entity, means the other selections are not
				// chosen.

				// Handle clicking on the tool palette.
				if (e.getSource() == toolPalette) {
					if (_PropogateChange) {
						_PropogateChange = false;
						if (entityPalette != null)
							entityPalette.clearSelection();
						if (tilePalette != null)
							tilePalette.clearSelection();
						_PropogateChange = true;
					}
					selections.tool = toolPalette.getSelectedValue();
				}
				// Handle clicking on the tile palette.
				else if (e.getSource() == tilePalette) {
					if (_PropogateChange) {
						_PropogateChange = false;
						toolPalette.clearSelection();
						entityPalette.clearSelection();
						_PropogateChange = true;
					}
					if (tilePalette.getSelectedValue() != null)
						selections.tileType = tilePalette.getSelectedValue();
					toolPalette.setSelectedValue(_TilePen, true);
				}
				// Handle clicking on the entity palette.
				else if (e.getSource() == entityPalette) {
					if (_PropogateChange) {
						_PropogateChange = false;
						toolPalette.clearSelection();
						tilePalette.clearSelection();
						_PropogateChange = true;
					}
					if (entityPalette.getSelectedValue() != null)
						selections.entityType = entityPalette.getSelectedValue();
					toolPalette.setSelectedValue(_EntityPlacer, true);
				}

			}

		}


		/** Handle changes in the state of the zoom slider. */
		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() instanceof JSlider) {
				JSlider sldr = (JSlider) e.getSource();
				if (sldr.getName().equals("zoomSlider")) {
					_ViewControl.setZoomAsPercentage((float) (sldr.getValue()) / sldr.getMaximum());
				}
			}
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {

			case "UNDO":
				Tool.undo(world);
				break;
			case "REDO":
				Tool.redo(world);
				break;
			case "Save to LevelPack":
				File saveLevelPackFile = FileControl.saveAsDialog(LevelEditorScreen.this);
				if (saveLevelPackFile == null)
					System.out.println("Save cancelled.");
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveLevelPackFile))) {
					String json = levelPack.toJson();
					writer.write(json);
					System.out.println("Save LevelPack complete.");
				} catch (IOException ioex) {
					ioex.printStackTrace();
				}
				return;
			case "SaveAs to LevelPack":
				System.err.println("Not implemented SaveAs.");
				return;
			case "Open LevelPack":
				File openLevelPackFile = FileControl.openDialog(LevelEditorScreen.this);
				if (openLevelPackFile == null)
					System.out.println("Open cancelled.");
				else if (openLevelPackFile.getName().endsWith(".json")) {
					LevelPack levelPack = LevelPack.fromFile(openLevelPackFile.getPath());
					DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen(levelPack));
					System.out.println("Open LevelPack complete.");
				} else {
					System.out.println("Unsupported file type: " + openLevelPackFile.getName());
				}
				return;
			case "Exit to Main":
				if (JOptionPane.showConfirmDialog(LevelEditorScreen.this, "Are you sure?", "Exit to Main",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());
				return;
			case "Quit":
				int dialogResult = JOptionPane.showConfirmDialog(LevelEditorScreen.this, "Are you sure?", "Quit",
						JOptionPane.YES_NO_OPTION);
				if (dialogResult == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
				return;
			case "Export":
				try {
					File f = FileControl.saveAsDialog(LevelEditorScreen.this);
					f.getParentFile().mkdirs();
					Files.write(Paths.get(f.getPath()), Serializer.serializeLevelPack(levelPack).getBytes());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return;
			case "Switch to Play":
				DungeonBotsMain.instance.setCurrentScreen(new GameplayScreen(levelPack, true));
				return;

			case "WORLD_SCRIPTS":
				JWorldEditor.create(LevelEditorScreen.this, world, "Edit your world...", new Undoable.Listener() {

					@Override
					public void pushUndoable(Undoable<?> u) {
						Tool.pushUndo(world, u);
					}
				});
				return;
			case "delete":
				Entity[] selectedEntities = _View.getSelectedEntities();
				if (selectedEntities == null || selectedEntities.length == 0)
					return;
				for (Entity entity : selectedEntities)
					world.removeEntity(entity);
				_View.setSelectedEntities(null);
				Undoable<Entity[]> u = new Undoable<Entity[]>(selectedEntities, null) {

					@Override
					protected void undoValidated() {
						for (Entity e : before)
							world.addEntity(e);
						_View.setSelectedEntities(before);
					}


					@Override
					protected void redoValidated() {
						for (Entity e : before)
							world.removeEntity(e);
						_View.setSelectedEntities(null);

					}


					@Override
					protected boolean okayToUndo() {
						for (Entity e : before)
							if (world.containsEntity(e))
								return false;
						return true;
					}


					@Override
					protected boolean okayToRedo() {
						for (Entity e : before)
							if (!world.containsEntity(e))
								return false;
						return true;
					}
				};
				Tool.pushUndo(world, u);
				return;
			default:
				System.out.println("LevelEditorScreen has not implemented the command: " + e.getActionCommand());
				return;
			}
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseClicked(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseDragged(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void mouseMoved(MouseEvent e) {
			if (selections.tool != null)
				selections.tool.mouseMoved(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void mouseEntered(MouseEvent e) {
			if (selections.tool != null)
				selections.tool.mouseEntered(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void mouseExited(MouseEvent e) {
			if (selections.tool != null)
				selections.tool.mouseExited(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mousePressed(e);
			if (e.isConsumed())
				return;

		}


		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseReleased(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (selections.tool != null)
				selections.tool.mouseWheelMoved(e);
			;
			/* if (_ViewControl != null) _ViewControl.mouseWheelMoved(e); */
		}

	}


	/** Handles the rendering of an item in the TilePen palette. */
	private static ListCellRenderer<TileType> _TileTypeItemRenderer = new ListCellRenderer<TileType>() {

		private final Font font = UIBuilder.getFont("DawnLike/GUI/SDS_8x8.ttf").deriveFont(12f);


		@Override
		public Component getListCellRendererComponent(JList<? extends TileType> list, TileType tileType, int index,
				boolean isSelected, boolean cellHasFocus) {
			JPanel pnl = new JPanel(new HorizontalLayout());
			pnl.setOpaque(false);
			JLabel lbl1 = UIBuilder.buildLabel()
					.image(tileType.getTexture().toImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)).create();
			JLabel lbl2 = UIBuilder.buildLabel().text(tileType.getName()).create();
			lbl2.setFont(font);
			if (isSelected || cellHasFocus)
				pnl.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
			else
				pnl.setBorder(new EmptyBorder(3, 3, 3, 3));
			// lbl2.setForeground(Color.red);
			pnl.add(lbl1);
			pnl.add(lbl2);
			return pnl;
		}
	};
	/** Handles the rendering of an item in the EntityPLacer palette. */
	private static ListCellRenderer<EntityType> _EntityItemRenderer = new ListCellRenderer<EntityType>() {

		private final Font font = UIBuilder.getFont("DawnLike/GUI/SDS_8x8.ttf").deriveFont(12f);


		@Override
		public Component getListCellRendererComponent(JList<? extends EntityType> list, EntityType entityType,
				int index, boolean isSelected, boolean cellHasFocus) {
			JPanel pnl = new JPanel(new HorizontalLayout());
			pnl.setOpaque(false);
			JLabel lbl1 = UIBuilder.buildLabel()
					.image(entityType.previewTexture.toImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)).create();
			JLabel lbl2 = UIBuilder.buildLabel().text(entityType.name).create();
			lbl2.setFont(font);
			if (isSelected || cellHasFocus)
				pnl.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
			else
				pnl.setBorder(new EmptyBorder(3, 3, 3, 3));
			// lbl2.setForeground(Color.red);
			pnl.add(lbl1);
			pnl.add(lbl2);
			return pnl;
		}
	};
	/** Handles the rendering of an item in the Tool palette. */
	private static ListCellRenderer<Tool> _ToolRenderer = new ListCellRenderer<Tool>() {

		@Override
		public Component getListCellRendererComponent(JList<? extends Tool> list, Tool tool, int index,
				boolean isSelected, boolean cellHasFocus) {
			ImageIcon icon = null;
			if (tool.image != null)
				icon = new ImageIcon(tool.image);
			JLabel lbl = new JLabel(icon);

			lbl.setText(tool.name);
			if (isSelected || cellHasFocus)
				lbl.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
			else
				lbl.setBorder(new EmptyBorder(3, 3, 3, 3));
			return lbl;
		}

	};


	/** Build the actual GUI for the Level Editor. */

	@SuppressWarnings("serial")
	@Override
	protected void addComponents(Container pane) {
		pane.setLayout(new BorderLayout());

		// Add the world at the bottom layer.
		_View = new WorldView(world,
				(w) -> {throw new RuntimeException("World cannot be won in level editor");} );
		_ViewControl = new Tool.ViewControl(_View);
		getController().registerSignalsFrom(_View);
		_View.setBounds(0, 0, this.getSize().width, this.getSize().height);
		_View.setOpaque(false);

		// Make the world responsive to "delete" key.
		InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = this.getRootPane().getActionMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		actionMap.put("delete", new AbstractAction() {

			// Jeez this is hackish
			@Override
			public void actionPerformed(ActionEvent e) {
				getController().actionPerformed(new ActionEvent(e.getSource(), e.getID(), "delete"));
			}
		});

		// Create the tool palette GUI.
		JList<Tool> toolList = ((Controller) getController()).toolPalette = new JList<Tool>();
		toolList.setCellRenderer(_ToolRenderer);
		toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		toolList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		toolList.setVisibleRowCount(-1);
		// JPanel toolCollapser = UIBuilder.makeCollapser(new
		// JScrollPane(toolList), "Tools", "Tools", "", false);
		JScrollPane toolScroller = new JScrollPane(toolList);
		toolScroller.setPreferredSize(new Dimension(150, 150));
		// Set up the members of the tool list
		toolList.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<Tool> tm = new DefaultListModel<Tool>();
		tm.addElement(_Selector = new Tool.Selector(_View, this, SecurityLevel.AUTHOR, _ViewControl)
				.setSelectsEntities(true).setSelectsTiles(true));
		tm.addElement(_TilePen = new Tool.TilePen(_View, selections, _ViewControl));
		tm.addElement(
				_EntityPlacer = new Tool.EntityPlacer(_View, selections, this, SecurityLevel.AUTHOR, _ViewControl));
		toolList.setModel(tm);
		toolList.setSelectedValue(selections.tool = _Selector, true);

		// Create the tile palette GUI.
		JList<TileType> tileTypeList = ((Controller) getController()).tilePalette = new JList<TileType>();
		tileTypeList.setCellRenderer(_TileTypeItemRenderer);
		tileTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tileTypeScroller = new JScrollPane(tileTypeList);
		tileTypeScroller.setPreferredSize(new Dimension(150, 250));
		// Set up the tile list.
		tileTypeList.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<TileType> im = new DefaultListModel<TileType>();
		for (TileType i : world.getTileTypes())
			im.addElement(i);
		tileTypeList.setModel(im);

		// Create the entity palette GUI.
		JList<EntityType> entityList = ((Controller) getController()).entityPalette = new JList<EntityType>();
		entityList.setCellRenderer(_EntityItemRenderer);
		entityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane entityScroller = new JScrollPane(entityList);
		entityScroller.setPreferredSize(new Dimension(150, 230));
		// JPanel entitiesCollapser = UIBuilder.makeCollapser(new
		// JScrollPane(entityList), "Entities", "Entities", "",
		// false);
		// Set up the entity list.
		entityList.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<EntityType> em = new DefaultListModel<EntityType>();
		for (EntityType e : createEntityTypes())
			em.addElement(e);
		entityList.setModel(em);


		// Create the zoom slider.
		JSlider zoomSlider = new JSlider();
		zoomSlider.setName("zoomSlider");
		zoomSlider.addChangeListener((ChangeListener) getController());
		zoomSlider.setBorder(BorderFactory.createTitledBorder("Zoom"));

		// Build the control panel.
		JPanel controlPanel = new JPanel();
		controlPanel.setFocusable(false);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
		controlPanel.add(zoomSlider);
		controlPanel.add(Box.createVerticalStrut(10));
		controlPanel.add(new JLabel("Tools"));
		controlPanel.add(toolScroller);
		controlPanel.add(Box.createVerticalStrut(10));
		controlPanel.add(new JLabel("Tile types"));
		controlPanel.add(tileTypeScroller);
		controlPanel.add(Box.createVerticalStrut(10));
		controlPanel.add(new JLabel("Entity types"));
		controlPanel.add(entityScroller);


		// Create the file menu
		JMenu fileMenu = UIBuilder.buildMenu().mnemonic('f').prefWidth(60).text("File").create();
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_S, ActionEvent.CTRL_MASK).mnemonic('s')
				.text("Save").action("Save to LevelPack", getController()).create());
		fileMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_S, ActionEvent.CTRL_MASK).mnemonic('a')
				.text("Save As...").action("SaveAs to LevelPack", getController()).create());
		fileMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_O, ActionEvent.CTRL_MASK).mnemonic('o')
				.text("Open").action("Open LevelPack", getController()).create());
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_X, ActionEvent.CTRL_MASK).mnemonic('x')
				.text("Exit to Main").action("Exit to Main", getController()).create());
		fileMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_Q, ActionEvent.CTRL_MASK).mnemonic('q')
				.text("Quit").action("Quit", getController()).create());

		// Create the world menu.
		JMenu worldMenu = UIBuilder.buildMenu().mnemonic('w').text("World").prefWidth(60).create();
		worldMenu.add(
				UIBuilder.buildMenuItem().mnemonic('d').text("Data").action("WORLD_DATA", getController()).create());
		worldMenu.add(UIBuilder.buildMenuItem().mnemonic('s').action("WORLD_SCRIPTS", getController()).text("Scripts")
				.create());

		// Create the edit menu.
		JMenu editMenu = UIBuilder.buildMenu().mnemonic('e').text("Edit").prefWidth(50).create();
		editMenu.add(UIBuilder.buildMenuItem().accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK))
				.text("Undo").action("UNDO", getController()).create());
		editMenu.add(UIBuilder.buildMenuItem().accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK))
				.text("Redo").action("REDO", getController()).create());

		// Create the publish menu.
		JMenu publishMenu = UIBuilder.buildMenu().mnemonic('p').text("Publish").prefWidth(60).create();
		publishMenu.add(UIBuilder.buildMenuItem().mnemonic('c').text("Choose Stats")
				.action("Choose Stats", getController()).create());
		publishMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_A, ActionEvent.CTRL_MASK).mnemonic('a')
				.text("Audience").action("Audience", getController()).create());
		publishMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_U, ActionEvent.CTRL_MASK).mnemonic('u')
				.text("Upload").action("Upload", getController()).create());

		// Create the help menu.
		JMenu helpMenu = UIBuilder.buildMenu().mnemonic('h').text("Help").prefWidth(60).create();
		helpMenu.add(UIBuilder.buildMenuItem().text("About").action("About", getController()).create());

		// Put together the main menu.
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(worldMenu);
		menuBar.add(publishMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		// TODO: is sticking a button in a menu bar apt to cause compatibility
		// issues?
		menuBar.add(UIBuilder.buildButton().text("Switch to Play").action("Switch to Play", getController()).create());

		// Put together the entire page
		pane.add(controlPanel, BorderLayout.LINE_START);
		pane.add(_View, BorderLayout.CENTER);
		menuBar.setPreferredSize(new Dimension(-1, 30));
		this.setJMenuBar(menuBar);
	}


	@Override
	protected void setDefaultLayout() {
		this.setSize(1024, 768);
		this.setLocationRelativeTo(null);
		this.setUndecorated(false);
		this.setTitle("Create a game or lesson...");
	}

}
