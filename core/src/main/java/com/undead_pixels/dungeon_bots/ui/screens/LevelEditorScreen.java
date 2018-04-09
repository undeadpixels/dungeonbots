/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import com.undead_pixels.dungeon_bots.scene.entities.*;
import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.EntityType;
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import com.undead_pixels.dungeon_bots.ui.JPermissionTree;
import com.undead_pixels.dungeon_bots.ui.JWorldEditor;
import com.undead_pixels.dungeon_bots.ui.JWorldSizer;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.WindowListenerAdapter;
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

	private static final int ICON_WIDTH = 30;
	private static final int ICON_HEIGHT = 30;
	private static final String COMMAND_SAVE_TO_LEVELPACK = "SAVE_TO_LEVELPACK";
	private static final String COMMAND_SAVEAS_TO_LEVELPACK = "SAVEAS_TO_LEVELPACK";
	private static final String COMMAND_PERMISSIONS = "EDIT_PERMISSIONS";
	private static final String COMMAND_RESIZE = "RESIZE_WORLD";
	private static final String COMMAND_RESET_VIEW = "RESET_VIEW";


	// Defined by Swing, don't change this:
	private static final String COMMAND_COMBOBOX_CHANGED = "comboBoxChanged";


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
	private JComponent _ToolScroller;
	private JComponent _TileScroller;
	private JComponent _EntityScroller;
	private JList<Tool> _Tools;
	private JToolBar _ToolBar;
	// private boolean areToolsActive = true;


	public LevelEditorScreen(LevelPack levelPack) {
		super(levelPack);
	}


	public LevelEditorScreen() {
		super(new LevelPack("My Level Pack", DungeonBotsMain.instance.getUser(), new World()));
	}


	@Override
	protected ScreenController makeController() {
		return new LevelEditorScreen.Controller();
	}


	private final EntityType[] entityTypes = createEntityTypes();


	/** Creates all the entity types available in this Level Editor. */
	public EntityType[] createEntityTypes() {
		ArrayList<EntityType> result = new ArrayList<EntityType>();

		// TODO - some of the names produced by lambdas might need to be changed
		// later

		result.add(new EntityType("fish", AssetManager.getTextureRegion("DawnLike/Characters/Aquatic0.png", 2, 1),
				(x, y) -> {
					// TODO - create new actual entity class
					return new DeletemeEntity(world,
							AssetManager.getTextureRegion("DawnLike/Characters/Aquatic0.png", 2, 1), x, y);
				}));
		result.add(new EntityType("demon", AssetManager.getTextureRegion("DawnLike/Characters/Demon0.png", 2, 3),
				(x, y) -> {
					// TODO - create new actual entity class
					return new DeletemeEntity(world,
							AssetManager.getTextureRegion("DawnLike/Characters/Demon0.png", 2, 3), x, y);
				}));
		result.add(new EntityType("ghost", AssetManager.getTextureRegion("DawnLike/Characters/Undead0.png", 2, 4),
				(x, y) -> {
					// TODO - create new actual entity class
					return new DeletemeEntity(world,
							AssetManager.getTextureRegion("DawnLike/Characters/Undead0.png", 2, 4), x, y);
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
		result.add(new EntityType("sign", Sign.DEFAULT_TEXTURE, (x, y) -> {
			return new Sign(world, "Please Recycle", x, y);
		}));
		result.add(new EntityType("gold", ItemEntity.GOLD_TEXTURE, (x, y) -> {
			return ItemEntity.gold(world, x, y, 2);
		}));
		result.add(new EntityType("gem", ItemEntity.GEM_TEXTURE, (x, y) -> {
			return ItemEntity.gem(world, x, y);
		}));
		result.add(new EntityType("diamond", ItemEntity.DIAMOND_TEXTURE, (x, y) -> {
			return ItemEntity.diamond(world, x, y);
		}));
		return result.toArray(new EntityType[result.size()]);
	}


	/**Updates the GUI state based on the current tile, tile, and entity selections.*/
	private void updateGUIState() {
		if (_ToolBar == null)
			return;

		_ToolScroller.setVisible(true);

		boolean hasTool = selections != null && selections.tool != null;
		boolean entitiesVisible = hasTool && selections.tool instanceof Tool.EntityPlacer;
		_EntityScroller.setVisible(entitiesVisible);

		boolean tilesVisible = hasTool && selections.tool instanceof Tool.TilePen;
		_TileScroller.setVisible(tilesVisible);
		_ToolBar.revalidate();
	}


	private void saveWhitelist(HashMap<String, SecurityLevel> permissions) {
		Whitelist whitelist = world.getWhitelist();
		Undoable<?> u = whitelist.setAllLevels(permissions);
		Tool.pushUndo(world, u);
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
		// public JList<TileType> tilePalette;
		/** The list managing the selection of an entity type. */
		// public JList<EntityType> entityPalette;

		// ===========================================================
		// ======== LevelEditorScreen.Controller TOOL STUFF
		// ===========================================================

		/**
		 * This stupid boolean is there just to flag a list's clearSelection()
		 * call from in turn clearing other lists.
		 */
		private transient boolean _PropogateChange = true;


		@SuppressWarnings("unchecked")
		private void tileSelectionChanged() {
			if (_PropogateChange) {
				_PropogateChange = false;
				toolPalette.setSelectedValue(_TilePen, true);
				_PropogateChange = true;
			}
			selections.tileType = (TileType) ((JComboBox<TileType>) _TileScroller).getSelectedItem();
			updateGUIState();
		}


		@SuppressWarnings("unchecked")
		private void entitySelectionChanged() {
			if (_PropogateChange) {
				_PropogateChange = false;
				toolPalette.setSelectedValue(_EntityPlacer, true);
				_PropogateChange = true;
			}
			selections.entityType = (EntityType) ((JComboBox<EntityType>) _EntityScroller).getSelectedItem();
			updateGUIState();
		}


		/** Handle the palette list changes. */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getSource() == toolPalette) {
				selections.tool = toolPalette.getSelectedValue();
				updateGUIState();
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


		private String filename = "";


		@Override
		public void actionPerformed(ActionEvent e) {


			switch (e.getActionCommand()) {

			case COMMAND_COMBOBOX_CHANGED:
				if (e.getSource() == _EntityScroller) {
					entitySelectionChanged();
				} else if (e.getSource() == _TileScroller) {
					tileSelectionChanged();
				} else
					assert false;// Sanity check
				return;
			case "UNDO":
				Tool.undo(world);
				return;
			case "REDO":
				Tool.redo(world);
				return;
			case COMMAND_PERMISSIONS:
				JPermissionTree jpe = JPermissionTree.createDialog(LevelEditorScreen.this, "Edit permissions",
						(permissions, infos) -> saveWhitelist(permissions));
				jpe.setItems(world.getWhitelist());
				jpe.setVisible(true);
				break;
			case COMMAND_RESET_VIEW:
				Point2D.Float worldSize = world.getSize();
				Point2D.Float center = new Point2D.Float(worldSize.x / 2, worldSize.y / 2);
				_ViewControl.setCenter(center);
				_ViewControl.setZoomAsPercentage(0.5f);
				break;
			case COMMAND_RESIZE:
				JWorldSizer jws = JWorldSizer.showDialog(LevelEditorScreen.this, world, _View);
				jws.getDialog().addWindowListener(new WindowListenerAdapter() {

					@Override
					protected void event(WindowEvent e) {
						if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
							return;
						_Tools.setEnabled(true);
					}
				});

				// No other tools should be available while the resizer is
				// working.
				_Tools.clearSelection();
				_Tools.setEnabled(false);
				break;
			case COMMAND_SAVE_TO_LEVELPACK:
				File sfd = new File(filename);
				if (!sfd.exists())
					sfd = FileControl.saveAsDialog(LevelEditorScreen.this);
				if (sfd == null)
					System.out.println("Save cancelled.");
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(sfd))) {
					String json = levelPack.toJson();
					writer.write(json);
					System.out.println("Save LevelPack complete.");
				} catch (IOException ioex) {
					ioex.printStackTrace();
				}
				return;
			case COMMAND_SAVEAS_TO_LEVELPACK:
				File safd = FileControl.saveAsDialog(LevelEditorScreen.this);
				if (safd == null)
					System.out.println("Save cancelled.");
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(safd))) {
					String json = levelPack.toJson();
					writer.write(json);
					System.out.println("Save LevelPack complete.");
				} catch (IOException ioex) {
					ioex.printStackTrace();
				}
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
				JWorldEditor jwe = JWorldEditor.createDialog(LevelEditorScreen.this, world, "Edit your world...",
						SecurityLevel.AUTHOR);
				if (jwe != null)
					jwe.setVisible(true);
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
			_ViewControl.mouseClicked(e);
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseDragged(e);
			if (e.isConsumed())
				return;
			_ViewControl.mouseDragged(e);
		}


		@Override
		public void mouseMoved(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseMoved(e);
			if (e.isConsumed())
				return;
			_ViewControl.mouseMoved(e);
		}


		@Override
		public void mouseEntered(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseEntered(e);
			if (e.isConsumed())
				return;
			_ViewControl.mouseEntered(e);
		}


		@Override
		public void mouseExited(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseExited(e);
			if (e.isConsumed())
				return;
			_ViewControl.mouseExited(e);
		}


		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mousePressed(e);
			if (e.isConsumed())
				return;
			_ViewControl.mousePressed(e);

		}


		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseReleased(e);
			if (e.isConsumed())
				return;
			_ViewControl.mouseReleased(e);
		}


		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getSource() != _View)
				return;
			if (selections.tool != null)
				selections.tool.mouseWheelMoved(e);
			if (e.isConsumed())
				return;
			_ViewControl.mouseWheelMoved(e);
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
			if (tileType != null) {
				JLabel lbl1 = UIBuilder.buildLabel().image(
						tileType.getTexture().toImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH))
						.create();
				JLabel lbl2 = UIBuilder.buildLabel().text(tileType.getName()).create();
				lbl2.setFont(font);
				pnl.add(lbl1);
				pnl.add(lbl2);
			}

			if (isSelected || cellHasFocus)
				pnl.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
			else
				pnl.setBorder(new EmptyBorder(3, 3, 3, 3));

			pnl.setPreferredSize(new Dimension(170,30));
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
			if (entityType != null) {
				JLabel lbl1 = UIBuilder.buildLabel().image(entityType.previewTexture.toImage()
						.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH)).create();
				JLabel lbl2 = UIBuilder.buildLabel().text(entityType.name).create();
				lbl2.setFont(font);
				pnl.add(lbl1);
				pnl.add(lbl2);
			}

			if (isSelected || cellHasFocus)
				pnl.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
			else
				pnl.setBorder(new EmptyBorder(3, 3, 3, 3));
			
			pnl.setPreferredSize(new Dimension(170,30));

			return pnl;
		}
	};
	/** Handles the rendering of an item in the Tool palette. */
	private static ListCellRenderer<Tool> _ToolRenderer = new ListCellRenderer<Tool>() {

		@Override
		public Component getListCellRendererComponent(JList<? extends Tool> list, Tool tool, int index,
				boolean isSelected, boolean cellHasFocus) {
			ImageIcon icon = null;
			if (tool.image != null) {
				icon = new ImageIcon(tool.image);
			}
			JLabel lbl = new JLabel(icon);
			lbl.setText(tool.name);
			if (isSelected || cellHasFocus)
				lbl.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.RED));
			else
				lbl.setBorder(new EmptyBorder(3, 3, 3, 3));
			lbl.setOpaque(true);
			return lbl;
		}
	};


	/** Build the actual GUI for the Level Editor. */

	@SuppressWarnings("serial")
	@Override
	protected void addComponents(Container pane) {
		pane.setLayout(new BorderLayout());

		// Add the world at the bottom layer.
		_View = new WorldView(world, (w) -> {
			throw new RuntimeException("World cannot be won in level editor");
		});
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
		_Tools = new JList<Tool>();
		((Controller) getController()).toolPalette = _Tools;
		_Tools.setCellRenderer(_ToolRenderer);
		_Tools.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_Tools.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		_Tools.setVisibleRowCount(-1);
		_ToolScroller = new JScrollPane(_Tools);
		_ToolScroller.setBorder(BorderFactory.createTitledBorder("Tools"));
		// Set up the members of the tool list
		_Tools.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<Tool> tm = new DefaultListModel<Tool>();
		tm.addElement(_Selector = new Tool.Selector(_View, this, SecurityLevel.AUTHOR).setSelectsEntities(true)
				.setSelectsTiles(true));
		tm.addElement(_TilePen = new Tool.TilePen(_View, selections));
		tm.addElement(_EntityPlacer = new Tool.EntityPlacer(_View, selections, this, SecurityLevel.AUTHOR));
		_Tools.setModel(tm);


		// Create the tile palette GUI.
		JComboBox<TileType> cboxTile = new JComboBox<TileType>(world.getTileTypes().toArray());
		cboxTile.setRenderer(_TileTypeItemRenderer);
		cboxTile.addActionListener(getController());
		_TileScroller = cboxTile;
		_TileScroller.setBorder(BorderFactory.createTitledBorder("Tile Types"));
		this.selections.tileType = (TileType) cboxTile.getSelectedItem();


		// Create the entity palette GUI.
		JComboBox<EntityType> cboxEntity = new JComboBox<EntityType>(entityTypes);
		cboxEntity.setRenderer(_EntityItemRenderer);
		cboxEntity.addActionListener(getController());
		_EntityScroller = cboxEntity;
		_EntityScroller.setBorder(BorderFactory.createTitledBorder("Entities"));
		this.selections.entityType = (EntityType) cboxEntity.getSelectedItem();


		// Create the zoom slider.
		JSlider zoomSlider = new JSlider();		
		zoomSlider.setName("zoomSlider");
		zoomSlider.addChangeListener((ChangeListener) getController());
		zoomSlider.setBorder(BorderFactory.createTitledBorder("Zoom"));

		// Build the control panel.
		_ToolBar = new JToolBar("Editor tools");
		// toolBar.setLayout(new VerticalLayout());
		_ToolBar.setOrientation(SwingConstants.VERTICAL);
		_ToolBar.setFocusable(false);
		_ToolBar.setFloatable(true);
		_ToolBar.add(UIBuilder.buildButton().image("icons/zoom.png").text("Center view").toolTip("Set view to center.")
				.action(COMMAND_RESET_VIEW, getController()).border(new EmptyBorder(10, 10, 10, 10)).create());
		_ToolBar.add(zoomSlider);
		_ToolBar.add(UIBuilder.buildButton().image("icons/arrow_switch.png").text("Switch to Play")
				.action("Switch to Play", getController()).border(new EmptyBorder(10, 10, 10, 10)).create());
		_ToolBar.addSeparator();
		_ToolBar.add(_ToolScroller);
		_ToolBar.add(_TileScroller);
		_ToolBar.add(_EntityScroller);
		_Tools.setSelectedValue(selections.tool = _Selector, true);


		// Create the file menu
		JMenu fileMenu = UIBuilder.buildMenu().mnemonic('f').prefWidth(60).text("File").create();
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_S, ActionEvent.CTRL_MASK).mnemonic('s')
				.text("Save").action(COMMAND_SAVE_TO_LEVELPACK, getController()).create());
		fileMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_S, ActionEvent.CTRL_MASK).mnemonic('a')
				.text("Save As...").action(COMMAND_SAVEAS_TO_LEVELPACK, getController()).create());
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
		worldMenu.add(UIBuilder.buildMenuItem().mnemonic('r').text("Resize").action(COMMAND_RESIZE, getController())
				.create());
		worldMenu.add(UIBuilder.buildMenuItem().mnemonic('p').text("Permissions")
				.action(COMMAND_PERMISSIONS, getController()).create());

		// Create the edit menu.
		JMenu editMenu = UIBuilder.buildMenu().mnemonic('e').text("Edit").prefWidth(60).create();
		editMenu.add(UIBuilder.buildMenuItem().accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK))
				.text("Undo").action("UNDO", getController()).create());
		editMenu.add(UIBuilder.buildMenuItem().accelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK))
				.text("Redo").action("REDO", getController()).create());

		/* // Create the publish menu. JMenu publishMenu =
		 * UIBuilder.buildMenu().mnemonic('p').text("Publish").prefWidth(60).
		 * create(); publishMenu.add(UIBuilder.buildMenuItem().mnemonic('c').
		 * text("Choose Stats") .action("Choose Stats",
		 * getController()).create());
		 * publishMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_A,
		 * ActionEvent.CTRL_MASK).mnemonic('a')
		 * .text("Audience").action("Audience", getController()).create());
		 * publishMenu.add(UIBuilder.buildMenuItem().accelerator(KeyEvent.VK_U,
		 * ActionEvent.CTRL_MASK).mnemonic('u') .text("Upload").action("Upload",
		 * getController()).create()); */

		// Create the help menu.
		JMenu helpMenu = UIBuilder.buildMenu().mnemonic('h').text("Help").prefWidth(60).create();
		helpMenu.add(UIBuilder.buildMenuItem().text("About").action("About", getController()).create());

		// Put together the main menu.
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(worldMenu);
		// menuBar.add(publishMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);

		// Put together the entire page
		// pane.add(controlPanel, BorderLayout.LINE_START);
		pane.add(_ToolBar, BorderLayout.LINE_START);
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
