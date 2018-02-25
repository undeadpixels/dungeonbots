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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
public class LevelEditorScreen extends Screen {

	/** The view */
	private WorldView _View;

	/** The visual list of tiles available for drawing in the editor. */
	private JList<TileType> _TilePalette;

	/** The visual list of entities available for dropping in the editor. */
	private JList<EntityType> _EntityPalette;

	/** The standard entity types. */
	private EntityType[] _EntityTypes = new EntityType[] { new EntityType("bot"), new EntityType("goal") };

	public LevelEditorScreen(World world) {
		super(world);

		DefaultListModel<TileType> lm = new DefaultListModel<TileType>();
		for (TileType t : world.getTileTypes())
			lm.addElement(t);
		_TilePalette.setModel(lm);

		DefaultListModel<EntityType> em = new DefaultListModel<EntityType>();
		for (EntityType e : _EntityTypes)
			em.addElement(e);
		_EntityPalette.setModel(em);

	}

	@Override
	protected ScreenController makeController() {
		return new ScreenController() {

			private File _CurrentFile = null;

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					// Draw to the world?
					if (e.getSource() == _View) {
						if (world == null)
							return;
						Object selection = _TilePalette.getSelectedValue();
						if (selection == null)
							return;
						if (selection instanceof TileType) {
							TileType drawType = (TileType) selection;
							// TileType currentTile = world.getTile(e.getX(),
							// e.getY());
							Point2D.Float gameCoords = _View.getScreenToGameCoords(e.getX(), e.getY());
							world.setTile((int) gameCoords.x, (int) gameCoords.y, drawType);
						}
						e.consume();
					}
				} else if (e.getClickCount() == 2) {
					if (e.getClickCount() == 2) {
						Point2D.Float gamePosition = _View.getScreenToGameCoords(e.getX(), e.getY());
						Entity entity = _View.getWorld().getEntityUnderLocation(gamePosition.x, gamePosition.y);
						if (entity == null)
							return;
						JEntityEditor.create(LevelEditorScreen.this, entity, SecurityLevel.AUTHOR, "Entity Editor");
						e.consume();
					}
				}

			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getSource() == _View) {
					if (world == null)
						return;
					Object selection = _TilePalette.getSelectedValue();
					if (selection == null)
						return;
					if (selection instanceof TileType) {
						TileType drawType = (TileType) selection;
						Point2D.Float gameCoords = _View.getCamera().unproject((float) e.getX(), (float) e.getY());
						world.setTile((int) gameCoords.x, (int) gameCoords.y, drawType);
					}
					e.consume();
				}
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getActionCommand()) {

				case "Save to LevelPack":
					LevelPack lp = DungeonBotsMain.instance.getLevelPack();
					String json = lp.toJson();
					Serializer.writeToFile("example.json",  json);
					break;
					/*
					 * // If there is not a cached file, treat as a SaveAs
					 * instead. if (_CurrentFile == null) _CurrentFile =
					 * FileControl.saveAsDialog(LevelEditorScreen.this); if
					 * (_CurrentFile != null) { String lua =
					 * world.getMapScript(); try (BufferedWriter writer = new
					 * BufferedWriter(new FileWriter(_CurrentFile))) {
					 * writer.write(lua); } catch (IOException e1) {
					 * e1.printStackTrace(); } } else
					 * System.out.println("Save cancelled."); break;
					 */
				case "Save As":
					System.err.println("SaveAs might be deprecated for LevelEditorScreen.");
					break;
				/*
				 * File saveFile =
				 * FileControl.saveAsDialog(LevelEditorScreen.this); if
				 * (saveFile != null) { String lua = world.getMapScript(); try
				 * (BufferedWriter writer = new BufferedWriter(new
				 * FileWriter(saveFile))) { writer.write(lua); } catch
				 * (IOException e1) { e1.printStackTrace(); } _CurrentFile =
				 * saveFile; } else System.out.println("SaveAs cancelled.");
				 * break;
				 */
				case "Import":
					File openFile = FileControl.openDialog(LevelEditorScreen.this);
					if (openFile != null) {
						World newWorld = new World(openFile);
						//LevelPack lp = DungeonBotsMain.instance.getLevelPack();
						//lp._levels.add(newWorld);
						//DungeonBotsMain.instance.setWorld(lp.levels.size() - 1);
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
					int dialogResult = JOptionPane.showConfirmDialog(LevelEditorScreen.this,
							"Would you like to save before quitting?", "Quit", JOptionPane.YES_NO_CANCEL_OPTION);
					if (dialogResult == JOptionPane.YES_OPTION) {
						if (_CurrentFile == null)
							_CurrentFile = FileControl.saveAsDialog(LevelEditorScreen.this);
						if (_CurrentFile != null) {
							String lua = world.getMapScript();
							try (BufferedWriter writer = new BufferedWriter(new FileWriter(_CurrentFile))) {
								writer.write(lua);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						} else
							System.out.println("Save cancelled.");
						System.exit(0);
					} else if (dialogResult == JOptionPane.NO_OPTION)
						System.exit(0);

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
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				System.out.println("Mouse pressed");
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent arg0) {

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {

			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowOpened(WindowEvent e) {
			}

		};
	}

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
	private ListSelectionListener _GreedySelectionListener = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			System.out.println(e.getFirstIndex() + " to " + e.getLastIndex());
			if (e.getValueIsAdjusting())
				return;
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

		// Create the palette, but don't add elements - this is done elsewhere.
		_TilePalette = new JList<TileType>();
		_TilePalette.setCellRenderer(_TileTypeItemRenderer);
		_TilePalette.setMinimumSize(new Dimension(150, 400));
		_TilePalette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//_TilePalette.addListSelectionListener(_GreedySelectionListener);
		JPanel tilesCollapser = UIBuilder.makeCollapser(new JScrollPane(_TilePalette), "Tiles", "Tiles", "", false);

		// Create the entity palette, but again don't add elements. That occurs
		// elsewhere.
		_EntityPalette = new JList<EntityType>();
		_EntityPalette.setCellRenderer(_EntityItemRenderer);
		_EntityPalette.setPreferredSize(new Dimension(150, 600));
		_EntityPalette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//_EntityPalette.addListSelectionListener(_GreedySelectionListener);
		JPanel entitiesCollapser = UIBuilder.makeCollapser(new JScrollPane(_EntityPalette), "Entities", "Entities", "",
				false);

		JPanel controlPanel = new JPanel();
		controlPanel.setFocusable(false);
		controlPanel.setLayout(new VerticalLayout());
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
