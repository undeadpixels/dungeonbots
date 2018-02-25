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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
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
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
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
@SuppressWarnings("serial")
public final class LevelEditorScreen extends Screen {

	private WorldView _View;
	private JList<Tool> _ToolPalette;
	private JList<TileType> _TilePalette;
	private JList<EntityType> _EntityPalette;
	private EntityType[] _EntityTypes = new EntityType[] { new EntityType("bot", UIBuilder.getImage("bot_entity.gif")),
			new EntityType("goal", UIBuilder.getImage("goal_entity.gif")) };

	public LevelEditorScreen(World world) {
		super(world);

		// The greedy selection listener will allow only one item to be selected
		// among the three lists.
		_GreedySelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {

				if (e.getSource() == _ToolPalette) {
					_EntityPalette.clearSelection();
					_TilePalette.clearSelection();
					((LevelEditorScreenController) getController()).currentTool = _ToolPalette.getSelectedValue();
				} else if (e.getSource() == _TilePalette) {
					_ToolPalette.clearSelection();
					_EntityPalette.clearSelection();
				} else if (e.getSource() == _EntityPalette) {
					_ToolPalette.clearSelection();
					_TilePalette.clearSelection();
				}
				LevelEditorScreenController controller = (LevelEditorScreenController) getController();
				controller.currentTool = _ToolPalette.getSelectedValue();
				controller.currentTileType = _TilePalette.getSelectedValue();
				controller.currentEntityType = _EntityPalette.getSelectedValue();
			}
		};

		// Set up the members of the lists.
		_ToolPalette.addListSelectionListener(_GreedySelectionListener);
		DefaultListModel<Tool> tm = new DefaultListModel<Tool>();
		for (Tool t : createTools())
			tm.addElement(t);
		_ToolPalette.setModel(tm);

		_TilePalette.addListSelectionListener(_GreedySelectionListener);
		DefaultListModel<TileType> lm = new DefaultListModel<TileType>();
		for (TileType t : world.getTileTypes())
			lm.addElement(t);
		_TilePalette.setModel(lm);

		_EntityPalette.addListSelectionListener(_GreedySelectionListener);
		DefaultListModel<EntityType> em = new DefaultListModel<EntityType>();
		for (EntityType e : _EntityTypes)
			em.addElement(e);
		_EntityPalette.setModel(em);
	}

	@Override
	protected ScreenController makeController() {
		return new LevelEditorScreenController();
	}

	protected class LevelEditorScreenController extends ScreenController {

		/**
		 * The current Tool provides alternative handlers for inputs. If a Tool
		 * is specified and the handler consumes an event, it will not be
		 * handled in this controller.
		 */
		public Tool currentTool = null;

		/** The current tile type for drawing. */
		public TileType currentTileType = null;

		/** The current entity type to place. */
		public EntityType currentEntityType = null;

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (currentTool != null)
				currentTool.mouseClicked(e);
			if (e.isConsumed())
				return;

			switch (e.getClickCount()) {
			case 1:
				TileType selection = currentTileType;
				if (selection == null)
					return;
				TileType drawType = (TileType) selection;
				Point2D.Float gameCoords = _View.getScreenToGameCoords(e.getX(), e.getY());
				world.setTile((int) gameCoords.x, (int) gameCoords.y, drawType);
				e.consume();
				break;
			case 2:
				Point2D.Float gamePosition = _View.getScreenToGameCoords(e.getX(), e.getY());
				Entity entity = _View.getWorld().getEntityUnderLocation(gamePosition.x, gamePosition.y);
				if (entity == null)
					return;
				JEntityEditor.create(LevelEditorScreen.this, entity, SecurityLevel.AUTHOR, "Entity Editor");
				e.consume();
				break;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.getSource() != _View)
				return;
			if (currentTool != null)
				currentTool.mouseDragged(e);
			if (e.isConsumed())
				return;
			Object selection = _TilePalette.getSelectedValue();
			if (selection == null)
				return;
			TileType drawType = (TileType) selection;
			Point2D.Float gameCoords = _View.getCamera().unproject((float) e.getX(), (float) e.getY());
			world.setTile((int) gameCoords.x, (int) gameCoords.y, drawType);
			e.consume();
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
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {

			case "Save to LevelPack":
				LevelPack lp = DungeonBotsMain.instance.getLevelPack();
				String json = lp.toJson();
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
					// LevelPack lp =
					// DungeonBotsMain.instance.getLevelPack();
					// lp._levels.add(newWorld);
					// DungeonBotsMain.instance.setWorld(lp.levels.size() -
					// 1);
					// _CurrentFile = openFile;
				} else
					System.out.println("Open cancelled.");

				break;
			case "Exit to Main":
				if (JOptionPane.showConfirmDialog(LevelEditorScreen.this, "Are you sure?", "Exit to Main",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					DungeonBotsMain.instance.setCurrentScreen(DungeonBotsMain.ScreenType.MAIN_MENU);
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
				break;
			default:
				System.out.println("Have not implemented the command: " + e.getActionCommand());
				break;
			}
		}

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

	/** Creates all the tools available in this controller. */
	public ArrayList<Tool> createTools() {
		ArrayList<Tool> result = new ArrayList<Tool>();
		result.add(new Tool("Selector", UIBuilder.getImage("selection.gif")) {

			Point cornerA = null;
			Point cornerB = null;

			@Override
			public void mousePressed(MouseEvent e) {
				if (cornerA != null)
					return;
				cornerB = cornerA = new Point(e.getX(), e.getY());
				if (_View.getRenderingTool() == null)
					_View.setRenderingTool(this);
				e.consume();
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				if (cornerA == null)
					return;

				Point2D.Float gamePositionA = _View.getScreenToGameCoords((int) cornerA.getX(), (int) cornerA.getY());
				Point2D.Float gamePositionB = _View.getScreenToGameCoords((int) cornerB.getX(), (int) cornerB.getY());
				int x1 = Math.min((int) gamePositionA.getX(), (int) gamePositionB.getX());
				int y1 = Math.min((int) gamePositionA.getY(), (int) gamePositionB.getY());
				int x2 = Math.max((int) gamePositionA.getX(), (int) gamePositionB.getX());
				int y2 = Math.max((int) gamePositionA.getY(), (int) gamePositionB.getY());

				if (_View.getRenderingTool() == this)
					_View.setRenderingTool(null);

				ArrayList<Point> selectedTiles = new ArrayList<Point>();
				for (int x = x1; x <= x2; x++) {
					for (int y = y1; y <= y2; y++) {
						selectedTiles.add(new Point(x, y));
					}
				}
				if (selectedTiles.size()==1){
					
				}
				
				_View.setSelectedTiles(selectedTiles.toArray(new Point[selectedTiles.size()]));

				cornerB = cornerA = null;
				e.consume();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (cornerA == null)
					return;
				cornerB = new Point(e.getX(), e.getY());
				e.consume();
			}

			@Override
			public void render(Graphics2D g) {
				int x = (int) Math.min(cornerA.getX(), cornerB.getX());
				int y = (int) Math.min(cornerA.getY(), cornerB.getY());
				int width = (int) Math.abs(cornerA.getX() - cornerB.getX());
				int height = (int) Math.abs(cornerA.getY() - cornerB.getY());
				g.setColor(Color.RED);
				g.setStroke(new BasicStroke(2));
				g.drawRect(x, y, width, height);
			}
		});

		return result;
	}

	/**
	 * A selection listener designed to ensure an item from only one list is
	 * selected at a time.
	 * <p>
	 * NOTE: cannot be designed at class declaration because non-static fields
	 * are not instantiated when the subclass constructor runs.
	 */
	private static ListSelectionListener _GreedySelectionListener = null;
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

	@Override
	protected void addComponents(Container pane) {
		pane.setLayout(new BorderLayout());

		// Add the world at the bottom layer.
		_View = new WorldView(world);
		_View.addMouseListener(getController());
		_View.addMouseMotionListener(getController());
		_View.setBounds(0, 0, this.getSize().width, this.getSize().height);
		_View.setOpaque(false);

		// The draw type combobox.
		JComboBox<String> brushBox = new JComboBox<String>();
		brushBox.addItem("Point");
		brushBox.addItem("Line");
		brushBox.addItem("Area");
		JPanel brushBoxContainer = new JPanel(new FlowLayout());
		brushBoxContainer.add(brushBox);
		brushBoxContainer.setMaximumSize(new Dimension(9999, 100));
		brushBoxContainer.setBorder(BorderFactory.createTitledBorder("Current draw mode"));

		// Create the tool palette, but the elements and selection listener will
		// be added elsewhere.
		_ToolPalette = new JList<Tool>();
		_ToolPalette.setCellRenderer(_ToolItemRenderer);
		_ToolPalette.setMinimumSize(new Dimension(150, 400));
		_ToolPalette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel toolCollapser = UIBuilder.makeCollapser(new JScrollPane(_ToolPalette), "Tools", "Tools", "", false);

		// Create the palette, but don't add elements - this is done elsewhere.
		_TilePalette = new JList<TileType>();
		_TilePalette.setCellRenderer(_TileTypeItemRenderer);
		_TilePalette.setMinimumSize(new Dimension(150, 400));
		_TilePalette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel tilesCollapser = UIBuilder.makeCollapser(new JScrollPane(_TilePalette), "Tiles", "Tiles", "", false);

		// Create the entity palette, but again don't add elements. That occurs
		// elsewhere.
		_EntityPalette = new JList<EntityType>();
		_EntityPalette.setCellRenderer(_EntityItemRenderer);
		_EntityPalette.setPreferredSize(new Dimension(150, 400));
		_EntityPalette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel entitiesCollapser = UIBuilder.makeCollapser(new JScrollPane(_EntityPalette), "Entities", "Entities", "",
				false);

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
