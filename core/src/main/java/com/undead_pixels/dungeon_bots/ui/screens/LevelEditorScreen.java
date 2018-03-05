/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
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
import com.undead_pixels.dungeon_bots.ui.JEntityEditor;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
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

	public LevelEditorScreen(LevelPack levelPack) {
		this.world = levelPack.getCurrentWorld();
		this.levelPack = levelPack;
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
	protected final class Controller extends ScreenController implements ListSelectionListener {

		/** The list managing the selection of a tool. */
		public JList<Tool> toolPalette;
		/** The list managing the selection of a tile type. */
		public JList<TileType> tilePalette;
		/** The list managing the selection of an entity type. */
		public JList<EntityType> entityPalette;

		/** The current Tool provides handlers for inputs. */
		private Tool currentTool = null;
		/**
		 * The current entity type to place. If the tool is the entity placer.
		 */
		private EntityType currentEntityType = null;
		/**
		 * The current tile that will be drawn if the tool is the tile drawing
		 * tool.
		 */
		private TileType currentTileType = null;

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

					currentTool = toolPalette.getSelectedValue();
					currentTileType = null;
					currentEntityType = null;
				}
				// Handle clicking on the tile palette.
				else if (e.getSource() == tilePalette) {
					if (_PropogateChange) {
						_PropogateChange = false;
						toolPalette.clearSelection();
						entityPalette.clearSelection();
						_PropogateChange = true;
					}					
					JList<TileType> tp = tilePalette;
					currentTileType = tilePalette.getSelectedValue();
					currentEntityType = null;
					//TODO:  any selected tile should be changed.
					//TODO:  tool should be changed to a tile draw tool.
				}
				// Handle clicking on the entity palette.
				else if (e.getSource() == entityPalette) {
					if (_PropogateChange) {
						_PropogateChange = false;
						toolPalette.clearSelection();
						tilePalette.clearSelection();
						_PropogateChange = true;
					}					
					currentTileType = null;
					currentEntityType = entityPalette.getSelectedValue();
					//TODO:  entity placer should become the new tool.
				}

			}

		}

		/** Handle changes in the state of the zoom slider. */
		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() instanceof JSlider) {
				JSlider sldr = (JSlider) e.getSource();
				if (sldr.getName().equals("zoomSlider")) {
					OrthographicCamera cam = _View.getCamera();
					if (cam != null) {
						cam.setZoomOnMinMaxRange((float) (sldr.getValue()) / sldr.getMaximum());
					}
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {

			case "Save to LevelPack":
				String json = levelPack.toJson();
				Serializer.writeToFile("example.json", json);
				break;
			/*
			 * // If there is not a cached file, treat as a SaveAs instead. if
			 * (_CurrentFile == null) _CurrentFile =
			 * FileControl.saveAsDialog(LevelEditorScreen.this); if
			 * (_CurrentFile != null) { String lua = world.getMapScript(); try
			 * (BufferedWriter writer = new BufferedWriter(new
			 * FileWriter(_CurrentFile))) { writer.write(lua); } catch
			 * (IOException e1) { e1.printStackTrace(); } } else
			 * System.out.println("Save cancelled."); break;
			 */
			case "Save As":
				System.err.println("SaveAs might be deprecated for LevelEditorScreen.");
				break;
			/*
			 * File saveFile = FileControl.saveAsDialog(LevelEditorScreen.this);
			 * if (saveFile != null) { String lua = world.getMapScript(); try
			 * (BufferedWriter writer = new BufferedWriter(new
			 * FileWriter(saveFile))) { writer.write(lua); } catch (IOException
			 * e1) { e1.printStackTrace(); } _CurrentFile = saveFile; } else
			 * System.out.println("SaveAs cancelled."); break;
			 */
			case "Import":
				File openFile = FileControl.openDialog(LevelEditorScreen.this);
				if (openFile != null) {
					// World newWorld = new World(openFile);
					if (openFile.getName().endsWith(".lua")) {
						DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen(
								new LevelPack("New Level", DungeonBotsMain.instance.getUser(), new World(openFile))));
					} else {
						LevelPack levelPack = LevelPack.fromFile(openFile.getPath());
						DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen(levelPack));
					}
				} else
					System.out.println("Open cancelled.");

				break;
			case "Exit to Main":
				if (JOptionPane.showConfirmDialog(LevelEditorScreen.this, "Are you sure?", "Exit to Main",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());
				break;
			case "Quit":
				/*
				 * int dialogResult =
				 * JOptionPane.showConfirmDialog(LevelEditorScreen.this,
				 * "Would you like to save before quitting?", "Quit",
				 * JOptionPane.YES_NO_CANCEL_OPTION); if (dialogResult ==
				 * JOptionPane.YES_OPTION) { if (_CurrentFile == null)
				 * _CurrentFile =
				 * FileControl.saveAsDialog(LevelEditorScreen.this); if
				 * (_CurrentFile != null) { String lua = world.getMapScript();
				 * try (BufferedWriter writer = new BufferedWriter(new
				 * FileWriter(_CurrentFile))) { writer.write(lua); } catch
				 * (IOException e1) { e1.printStackTrace(); } } else
				 * System.out.println("Save cancelled."); System.exit(0); } else
				 * if (dialogResult == JOptionPane.NO_OPTION) System.exit(0);
				 */

				break;
			case "Export":
				try {
					File f = FileControl.saveAsDialog(LevelEditorScreen.this);
					f.getParentFile().mkdirs();
					Files.write(Paths.get(f.getPath()), Serializer.serializeLevelPack(levelPack).getBytes());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;
			default:
				System.out.println("Have not implemented the command: " + e.getActionCommand());
				break;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (currentTool != null)
				currentTool.mouseClicked(e);
			if (e.isConsumed())
				return;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (currentTool != null)
				currentTool.mouseDragged(e);
			if (e.isConsumed())
				return;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseMoved(e);
			if (e.isConsumed())
				return;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseEntered(e);
			if (e.isConsumed())
				return;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (currentTool != null)
				currentTool.mouseExited(e);
			if (e.isConsumed())
				return;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (currentTool != null)
				currentTool.mousePressed(e);
			if (e.isConsumed())
				return;

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (currentTool != null)
				currentTool.mouseReleased(e);
			if (e.isConsumed())
				return;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (currentTool != null)
				currentTool.keyPressed(e);
			if (e.isConsumed())
				return;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (currentTool != null)
				currentTool.keyPressed(e);
			if (e.isConsumed())
				return;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			if (currentTool != null)
				currentTool.keyPressed(e);
			if (e.isConsumed())
				return;
		}

	}

	/** Handles the rendering of an item in the TileType palette. */
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
	/** Handles the rendering of an item in the EntityType palette. */
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
	@Override
	protected void addComponents(Container pane) {
		pane.setLayout(new BorderLayout());

		// Add the world at the bottom layer.
		_View = new WorldView(world);
		_View.addMouseListener(getController());
		_View.addMouseMotionListener(getController());
		_View.setBounds(0, 0, this.getSize().width, this.getSize().height);
		_View.setOpaque(false);

		// Create the tool palette GUI.
		JList<Tool> tl = ((Controller) getController()).toolPalette = new JList<Tool>();
		tl.setCellRenderer(_ToolItemRenderer);
		tl.setMinimumSize(new Dimension(150, 400));
		tl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel toolCollapser = UIBuilder.makeCollapser(new JScrollPane(tl), "Tools", "Tools", "", false);
		// Set up the members of the lists.
		tl.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<Tool> tm = new DefaultListModel<Tool>();
		tm.addElement(new Tool.Selector(world,  _View,  this));
		tl.setModel(tm);

		// Create the tile palette GUI.
		JList<TileType> il = ((Controller) getController()).tilePalette = new JList<TileType>();
		il.setCellRenderer(_TileTypeItemRenderer);
		il.setMinimumSize(new Dimension(150, 400));
		il.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel tilesCollapser = UIBuilder.makeCollapser(new JScrollPane(il), "Tiles", "Tiles", "", false);
		// Set up the tile list.
		il.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<TileType> im = new DefaultListModel<TileType>();
		for (TileType i : world.getTileTypes())
			im.addElement(i);
		il.setModel(im);

		// Create the entity palette GUI.
		JList<EntityType> el = ((Controller) getController()).entityPalette = new JList<EntityType>();
		el.setCellRenderer(_EntityItemRenderer);
		el.setPreferredSize(new Dimension(150, 400));
		el.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel entitiesCollapser = UIBuilder.makeCollapser(new JScrollPane(el), "Entities", "Entities", "", false);
		// Set up the entity list.
		el.addListSelectionListener((LevelEditorScreen.Controller) getController());
		DefaultListModel<EntityType> em = new DefaultListModel<EntityType>();
		for (EntityType e : createEntityTypes())
			em.addElement(e);
		el.setModel(em);

		JPanel controlPanel = new JPanel();
		controlPanel.setFocusable(false);
		controlPanel.setLayout(new VerticalLayout());
		controlPanel.add(toolCollapser);
		controlPanel.add(tilesCollapser);
		controlPanel.add(entitiesCollapser);

		// Create the file menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setPreferredSize(new Dimension(50, 25));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(UIBuilder.makeMenuItem("Import", KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK),
				KeyEvent.VK_I, getController()));
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.makeMenuItem("Save to LevelPack",
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), KeyEvent.VK_S, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Save to Stand-Alone", getController()));
		/*
		 * fileMenu.add(UIBuilder.makeMenuItem("Save As",
		 * KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK |
		 * ActionEvent.ALT_MASK), 0, getController()));
		 */
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.makeMenuItem("Export", KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK),
				KeyEvent.VK_E, getController()));
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.makeMenuItem("Exit to Main",
				KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), KeyEvent.VK_X, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Quit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK),
				KeyEvent.VK_Q, getController()));

		// Create the world menu.
		JMenu worldMenu = new JMenu("World");
		worldMenu.setMnemonic(KeyEvent.VK_B);
		worldMenu.setPreferredSize(new Dimension(50, 25));
		worldMenu.add(UIBuilder.makeMenuItem("Data", KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK),
				KeyEvent.VK_D, getController()));
		worldMenu.add(UIBuilder.makeMenuItem("Scripts", null, KeyEvent.VK_S, getController()));

		// Create the publish menu.
		JMenu publishMenu = new JMenu("Publish");
		publishMenu.setMnemonic(KeyEvent.VK_P);
		publishMenu.setPreferredSize(new Dimension(50, 25));
		publishMenu.add(UIBuilder.makeMenuItem("Choose Stats", null, KeyEvent.VK_C, getController()));
		publishMenu.add(UIBuilder.makeMenuItem("Audience", KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK),
				KeyEvent.VK_A, getController()));
		publishMenu.add(UIBuilder.makeMenuItem("Upload", KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK),
				KeyEvent.VK_U, getController()));

		// Create the help menu.
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		helpMenu.setPreferredSize(new Dimension(50, 30));
		helpMenu.add(UIBuilder.makeMenuItem("About", null, 0, getController()));

		// Create the zoom slider.
		JSlider zoomSlider = new JSlider();
		zoomSlider.setName("zoomSlider");
		zoomSlider.addChangeListener(getController());
		zoomSlider.setBorder(BorderFactory.createTitledBorder("Zoom"));

		// Put together the main menu.
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(worldMenu);
		menuBar.add(publishMenu);
		menuBar.add(helpMenu);
		menuBar.add(zoomSlider);

		// Put together the entire page
		pane.add(controlPanel, BorderLayout.LINE_START);
		pane.add(menuBar, BorderLayout.PAGE_START);
		pane.add(_View, BorderLayout.CENTER);

	}

	@Override
	protected void setDefaultLayout() {
		this.setSize(1024, 768);
		this.setLocationRelativeTo(null);
		Image img = UIBuilder.getImage("LevelEditorScreen_background.jpg");
		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);
			this.setContentPane(new JLabel(new ImageIcon(img)));
		}
		this.setUndecorated(false);
		this.setTitle("Create a game or lesson...");
	}

}
