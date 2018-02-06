/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.event.ChangeEvent;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.ui.WorldView;

import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

/**
 * The screen for the level editor
 * 
 * @author Wesley
 *
 */
@SuppressWarnings("serial")
public class LevelEditorScreen extends Screen {

	/**
	 * The view
	 */
	private WorldView view;
	private World world;
	private JList<Object> _PaletteSelector;

	/**
	 * Current state. Used to update the world and write to file.
	 */
	private GameEditorState state;

	public LevelEditorScreen() {
		super();
		world = DungeonBotsMain.instance.getWorld();

		DefaultListModel<Object> lm = new DefaultListModel<Object>();
		for (TileType t : world.getTileTypes())
			lm.addElement(t);
		_PaletteSelector.setModel(lm);
		_PaletteSelector.validate();
		// TODO: add other types of elements.

		view.addMouseMotionListener(getController());
	}

	@Override
	protected ScreenController makeController() {
		return new ScreenController() {

			private File _CurrentFile = null;

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getSource() == view) {
					if (world == null)
						return;
					Object selection = _PaletteSelector.getSelectedValue();
					if (selection == null)
						return;
					if (selection instanceof TileType) {
						TileType drawType = (TileType) selection;
						TileType currentTile = world.getTile(e.getX(), e.getY());
						world.setTile(e.getX(), e.getY(), drawType);
					}
					e.consume();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getSource() == view) {
					if (world == null)
						return;
					Object selection = _PaletteSelector.getSelectedValue();
					if (selection == null)
						return;
					if (selection instanceof TileType) {
						// TODO: This is very hack-like. Work out the math.
						TileType drawType = (TileType) selection;
						TileType currentTile = world.getTile(e.getX(), e.getY());
						world.setTile(e.getX(), e.getY(), drawType);
					}
					e.consume();
				}
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

				switch (e.getActionCommand()) {

				case "Save":
					// If there is not a cached file, treat as a SaveAs instead.
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
					break;
				case "Save As":
					File saveFile = FileControl.saveAsDialog(LevelEditorScreen.this);
					if (saveFile != null) {
						String lua = world.getMapScript();
						try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
							writer.write(lua);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						_CurrentFile = saveFile;
					} else
						System.out.println("SaveAs cancelled.");
					break;
				case "Open":
					File openFile = FileControl.openDialog(LevelEditorScreen.this);
					if (openFile != null) {
						World newWorld = new World(openFile);
						DungeonBotsMain.instance.setWorld(newWorld);
						_CurrentFile = openFile;
					} else
						System.out.println("Open cancelled.");

					break;
				case "Exit to Main":
					if (JOptionPane.showConfirmDialog(LevelEditorScreen.this, "Are you sure?", "Exit to Main",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());
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
						OrthographicCamera cam = view.getCamera();
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
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

		};
	}

	/** Handles the rendering of TileTypes in the TileType palette. */
	private class PaletteItemRenderer extends DefaultListCellRenderer {
		// As suggested by "SeniorJD",
		// https://stackoverflow.com/questions/18896345/writing-a-custom-listcellrenderer,
		// Sep. 19, 2013

		@Override
		public Component getListCellRendererComponent(JList<? extends Object> list, Object item, int index,
				boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, item, index, isSelected, cellHasFocus);
			if (c instanceof JLabel) {
				// Are there any cases where a JLabel is not returned by
				// DefaultListCellRenderer?
				JLabel lbl = (JLabel) c;
				if (item instanceof TileType) {
					TileType tt = (TileType) item;
					lbl.setText(tt.getName());
				} else
					lbl.setText(item.toString());
			} else
				System.err.println("Unexpected component type returned in " + this.getClass().getName() + ":"
						+ c.getClass().getName());
			return c;
		}
	}

	@Override
	protected void addComponents(Container pane) {
		pane.setLayout(new BorderLayout());

		// Add the world at the bottom layer.
		view = new WorldView(DungeonBotsMain.instance.getWorld());
		view.addMouseListener(getController());
		view.setBounds(0, 0, this.getSize().width, this.getSize().height);
		view.setOpaque(false);

		// The draw type combobox.
		JComboBox<String> brushBox = new JComboBox<String>();
		brushBox.addItem("Point");
		brushBox.addItem("Line");
		brushBox.addItem("Area");
		JPanel brushBoxContainer = new JPanel(new FlowLayout());
		brushBoxContainer.add(brushBox);
		brushBoxContainer.setMaximumSize(new Dimension(9999, 100));
		brushBoxContainer.setBorder(BorderFactory.createTitledBorder("Current draw mode"));

		// Create the palette, but don't add elements - there's no ref to world
		// yet.
		_PaletteSelector = new JList<Object>();
		_PaletteSelector.setCellRenderer(new PaletteItemRenderer());
		_PaletteSelector.setPreferredSize(new Dimension(150, 300));
		JScrollPane paletteScroller = new JScrollPane(_PaletteSelector);
		paletteScroller.setBorder(BorderFactory.createTitledBorder("Current Tile"));

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
		controlPanel.add(paletteScroller);
		controlPanel.add(brushBoxContainer);
		controlPanel.add(new JLabel("Game position:"));
		controlPanel.add(new JLabel("Associated World scripts:"));
		controlPanel.add(new JLabel("Associated lines of code:"));
		controlPanel.add(new JLabel("Queue size:"));

		// Create the file menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setPreferredSize(new Dimension(50, 25));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(UIBuilder.makeMenuItem("Open", KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
				KeyEvent.VK_O, getController()));
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.makeMenuItem("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
				KeyEvent.VK_S, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Save As",
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK), 0,
				getController()));
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
		pane.add(view, BorderLayout.CENTER);

	}

	@Override
	protected void setDefaultLayout() {
		this.setSize(1024, 768);
		this.setLocationRelativeTo(null);
		Image img = DungeonBotsMain.getImage("LevelEditorScreen_background.jpg");

		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);

			this.setContentPane(new JLabel(new ImageIcon(img)));
		}
		this.setUndecorated(false);
		this.setTitle("Create a game or lesson...");

	}

}
