/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.scene.EntityType;
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.JCodeEditorPaneController;
import com.undead_pixels.dungeon_bots.ui.JEntityEditor;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.WindowListenerAdapter;
import com.undead_pixels.dungeon_bots.ui.WorldView;

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
	protected final World world;
	protected final LevelPack levelPack;
	public final Tool.SelectionModel selections = new Tool.SelectionModel();

	// Special tools
	private Tool.Selector _Selector;
	private Tool.TilePen _TilePen;
	private Tool.EntityPlacer _EntityPlacer;
	private Tool.ViewControl _ViewControl;


	public LevelEditorScreen(LevelPack levelPack) {
		this.levelPack = levelPack;
		this.world = levelPack.getCurrentWorld();
	}


	@Override
	protected ScreenController makeController() {
		return new LevelEditorScreen.Controller();
	}


	/** Creates all the entity types available in this Level Editor. */
	public ArrayList<EntityType> createEntityTypes() {
		ArrayList<EntityType> result = new ArrayList<EntityType>();
		result.add(new EntityType("bot", UIBuilder.getImage("bot_entity.gif")));
		result.add(new EntityType("goal", UIBuilder.getImage("goal_entity.gif")));
		return result;
	}


	/**
	 * The Level Editor controller works by maintaining a reference to a Tool to
	 * correctly handle inputs in the game view, whereas the controller itself
	 * handles Level Editor-related actions.
	 */
	protected final class Controller extends ScreenController implements ListSelectionListener, MouseWheelListener {

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
						entityPalette.clearSelection();
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
			case "Save to Stand-Alone":
				File saveStandAloneFile = FileControl.saveAsDialog(LevelEditorScreen.this);
				if (saveStandAloneFile == null)
					return;
				String lua = world.getMapScript();
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveStandAloneFile))) {
					writer.write(lua);
					System.out.println("Save to Stand-Alone complete.");
				} catch (IOException ioex) {
					ioex.printStackTrace();
				}
				return;
			case "Open Stand-Alone":
				File openStandAloneFile = FileControl.openDialog(LevelEditorScreen.this);
				if (openStandAloneFile == null) {
					System.out.println("Open cancelled.");
					return;
				} else if (openStandAloneFile.getName().endsWith(".lua")) {
					DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen(new LevelPack("New Level",
							DungeonBotsMain.instance.getUser(), new World(openStandAloneFile))));
					System.out.println("Open from Stand-Alone complete.");
				} else
					System.out.println("Unsupported Stand-Alone file type: " + openStandAloneFile.getName());
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
			case "WORLD_SCRIPTS":
				
				//JComponent scriptEditor = new JCodeEditorPaneController(world, SecurityLevel.AUTHOR).create();

				//JDialog dialog = new JDialog(LevelEditorScreen.this, "Level Scripts", Dialog.ModalityType.MODELESS);
				//dialog.add(scriptEditor);
				//dialog.pack();
				//dialog.setVisible(true);
				
				//return;
			default:
				System.out.println("Have not implemented the command: " + e.getActionCommand());
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
		public void keyPressed(KeyEvent e) {
			if (selections.tool != null)
				selections.tool.keyPressed(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void keyReleased(KeyEvent e) {
			if (selections.tool != null)
				selections.tool.keyPressed(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void keyTyped(KeyEvent e) {
			if (selections.tool != null)
				selections.tool.keyPressed(e);
			if (e.isConsumed())
				return;
		}


		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (_ViewControl != null)
				_ViewControl.mouseWheelMoved(e);
		}

	}


	/** Handles the rendering of an item in the TilePen palette. */
	private static ListCellRenderer<TileType> _TileTypeItemRenderer = new ListCellRenderer<TileType>() {

		@Override
		public Component getListCellRendererComponent(JList<? extends TileType> list, TileType item, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel lbl = new JLabel();
			lbl.setText(item.getName());
			if (isSelected || cellHasFocus)
				lbl.setForeground(Color.red);
			return lbl;
		}
	};
	/** Handles the rendering of an item in the EntityPLacer palette. */
	private static ListCellRenderer<EntityType> _EntityItemRenderer = new ListCellRenderer<EntityType>() {

		@Override
		public Component getListCellRendererComponent(JList<? extends EntityType> list, EntityType item, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel lbl = new JLabel();
			lbl.setText(item.name);
			if (isSelected || cellHasFocus)
				lbl.setForeground(Color.red);
			return lbl;
		}
	};
	/** Handles the rendering of an item in the Tool palette. */
	private static ListCellRenderer<Tool> _ToolItemRenderer = new ListCellRenderer<Tool>() {

		@Override
		public Component getListCellRendererComponent(JList<? extends Tool> list, Tool item, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel lbl = new JLabel();
			lbl.setText(item.name);
			if (isSelected || cellHasFocus)
				lbl.setForeground(Color.red);
			return lbl;
		}

	};


	/** Build the actual GUI for the Level Editor. */
	@SuppressWarnings("deprecation")
	@Override

	protected void addComponents(Container pane) {
		pane.setLayout(new BorderLayout());

		// Add the world at the bottom layer.
		_View = new WorldView(world);
		_View.addMouseListener(getController());
		_View.addMouseMotionListener(getController());
		_View.setBounds(0, 0, this.getSize().width, this.getSize().height);
		_View.setOpaque(false);
		_ViewControl = new Tool.ViewControl(_View);
		_View.addMouseWheelListener(_ViewControl);


		// Create the tile palette GUI.
		JList<TileType> tileTypeList = ((Controller) getController()).tilePalette = new JList<TileType>();
		tileTypeList.setCellRenderer(_TileTypeItemRenderer);
		tileTypeList.setMinimumSize(new Dimension(150, 400));
		tileTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel tilesCollapser = UIBuilder.makeCollapser(new JScrollPane(tileTypeList), "Tiles", "Tiles", "", false);
		// Set up the tile list.
		tileTypeList.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<TileType> im = new DefaultListModel<TileType>();
		for (TileType i : world.getTileTypes())
			im.addElement(i);
		tileTypeList.setModel(im);

		// Create the entity palette GUI.
		JList<EntityType> entityList = ((Controller) getController()).entityPalette = new JList<EntityType>();
		entityList.setCellRenderer(_EntityItemRenderer);
		entityList.setPreferredSize(new Dimension(150, 400));
		entityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel entitiesCollapser = UIBuilder.makeCollapser(new JScrollPane(entityList), "Entities", "Entities", "",
				false);
		// Set up the entity list.
		entityList.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<EntityType> em = new DefaultListModel<EntityType>();
		for (EntityType e : createEntityTypes())
			em.addElement(e);
		entityList.setModel(em);

		// Create the tool palette GUI.
		JList<Tool> toolList = ((Controller) getController()).toolPalette = new JList<Tool>();
		toolList.setCellRenderer(_ToolItemRenderer);
		toolList.setMinimumSize(new Dimension(150, 400));
		toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel toolCollapser = UIBuilder.makeCollapser(new JScrollPane(toolList), "Tools", "Tools", "", false);
		// Set up the members of the lists.
		toolList.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<Tool> tm = new DefaultListModel<Tool>();
		tm.addElement(_Selector = new Tool.Selector(_View, this, SecurityLevel.AUTHOR, _ViewControl));
		tm.addElement(_TilePen = new Tool.TilePen(_View, selections, _ViewControl));
		tm.addElement(
				_EntityPlacer = new Tool.EntityPlacer(_View, selections, this, SecurityLevel.AUTHOR, _ViewControl));
		toolList.setModel(tm);
		toolList.setSelectedValue(selections.tool = _Selector, true);

		// Create the zoom slider.
		JSlider zoomSlider = new JSlider();
		zoomSlider.setName("zoomSlider");
		zoomSlider.addChangeListener(getController());
		zoomSlider.setBorder(BorderFactory.createTitledBorder("Zoom"));

		// Build the control panel.
		JPanel controlPanel = new JPanel();
		controlPanel.setFocusable(false);
		controlPanel.setLayout(new VerticalLayout());
		controlPanel.add(zoomSlider);
		controlPanel.add(toolCollapser);
		controlPanel.add(tilesCollapser);
		controlPanel.add(entitiesCollapser);


		// Create the file menu
		JMenu fileMenu = UIBuilder.buildMenu().mnemonic('f').prefWidth(60).text("File").create();
		fileMenu.addSeparator();		
		fileMenu.add(UIBuilder.makeMenuItem("Save to LevelPack",
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), KeyEvent.VK_S, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Open LevelPack",
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), KeyEvent.VK_O, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Save to Stand-Alone", getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Open Stand-Alone", getController()));
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.makeMenuItem("Import", KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK),
				KeyEvent.VK_I, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Export", KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK),
				KeyEvent.VK_E, getController()));
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.makeMenuItem("Exit to Main",
				KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), KeyEvent.VK_X, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Quit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK),
				KeyEvent.VK_Q, getController()));

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
		publishMenu.add(UIBuilder.makeMenuItem("Choose Stats", null, KeyEvent.VK_C, getController()));
		publishMenu.add(UIBuilder.makeMenuItem("Audience", KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK),
				KeyEvent.VK_A, getController()));
		publishMenu.add(UIBuilder.makeMenuItem("Upload", KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK),
				KeyEvent.VK_U, getController()));

		// Create the help menu.
		JMenu helpMenu = UIBuilder.buildMenu().mnemonic('h').text("Help").prefWidth(60).create();
		helpMenu.add(UIBuilder.makeMenuItem("About", null, 0, getController()));

		// Put together the main menu.
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(worldMenu);
		menuBar.add(publishMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);

		// Put together the entire page
		pane.add(controlPanel, BorderLayout.LINE_START);
		pane.add(_View, BorderLayout.CENTER);
		menuBar.setPreferredSize(new Dimension(-1,30));
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
