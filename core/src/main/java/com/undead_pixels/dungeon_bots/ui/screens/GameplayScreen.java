package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Bot;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.JEntityEditor;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.WorldView;

/**
 * A screen for gameplay
 */
public class GameplayScreen extends Screen {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The JComponent that views the current world state. */
	private WorldView view;
	protected final World world;


	public GameplayScreen(World world) {
		this.world = world;
	}


	/**
	 * On closing the screen, each open editor will also need to be closed.
	 * Additionally, it makes no sense to open an editor twice for the same
	 * Entity. That's why the open editors are tracked.
	 *//* private final HashMap<Entity, JDialog> _OpenEditors = new
		 * HashMap<Entity, JDialog>(); */

	@Override
	protected ScreenController makeController() {

		return new ScreenController() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Point2D.Float gamePosition = view.getScreenToGameCoords(e.getX(), e.getY());
					Entity entity = view.getWorld().getEntityUnderLocation(gamePosition.x, gamePosition.y);
					if (entity == null)
						return;
					JEntityEditor.create(GameplayScreen.this, entity, SecurityLevel.DEFAULT, "Entity Editor", null);
					e.consume();
				}
			}


			/** Called when the zoom slider's state changes. */
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
			public void actionPerformed(ActionEvent e) {
				switch (e.getActionCommand()) {

				case "Open":
					File openFile = FileControl.openDialog(GameplayScreen.this);
					if (openFile != null) {
						if (openFile.getName().endsWith(".lua")) {
							DungeonBotsMain.instance.setCurrentScreen(new GameplayScreen(new World(openFile)));
						} else {
							LevelPack levelPack = LevelPack.fromFile(openFile.getPath());
							DungeonBotsMain.instance.setCurrentScreen(new GameplayScreen(levelPack.getCurrentWorld()));
						}
					}

					break;
				case "Exit to Main":
					if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", e.getActionCommand(),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());

					break;
				case "Quit":
					if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", e.getActionCommand(),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						System.exit(0);
					break;

				case "REWIND":
				case "Rewind":
					if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", e.getActionCommand(),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						world.reset();
					}
					break;

				case "PLAY":
				case "Play":
					world.beginPlay();
					break;
				case "Save":
				case "Save As":
				case "Stop":

				case "Last Result":
				case "Statistics":
				case "Upload":

				default:
					System.out.println("Have not implemented the command: " + e.getActionCommand());
					break;
				}
			}


			@Override
			public void mouseEntered(MouseEvent arg0) {
			}


			@Override
			public void mouseExited(MouseEvent arg0) {
			}


			@Override
			public void mousePressed(MouseEvent e) {

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
			public void mouseDragged(MouseEvent arg0) {
			}


			@Override
			public void mouseMoved(MouseEvent arg0) {
			}


		};

	}


	@Override
	protected void addComponents(Container pane) {

		pane.setLayout(new BorderLayout());

		// At the world at the bottom layer.
		view = new WorldView(world);
		view.addMouseListener(getController());
		view.setBounds(0, 0, this.getSize().width, this.getSize().height);
		view.setOpaque(false);

		// Set up the toolbar, which will be at the bottom of the screen
		JToolBar playToolBar = new JToolBar();
		playToolBar.setOpaque(false);
		JButton playBttn = UIBuilder.buildButton().image("icons/play.png").toolTip("Start the game.")
				.action("PLAY", getController()).preferredSize(50, 50).create();
		JButton stopBttn = UIBuilder.buildButton().image("icons/stop.png").toolTip("Stop the game.")
				.action("STOP", getController()).preferredSize(50, 50).create();
		JButton rewindBttn = UIBuilder.buildButton().image("icons/rewind.png").toolTip("Rewind the game.")
				.action("REWIND", getController()).preferredSize(50, 50).create();
		JSlider zoomSlider = new JSlider();
		zoomSlider.setName("zoomSlider");
		zoomSlider.addChangeListener(getController());
		zoomSlider.setBorder(BorderFactory.createTitledBorder("Zoom"));
		JPanel arrowPanel = new JPanel();
		arrowPanel.setLayout(new GridLayout(3, 3));
		arrowPanel.add(new JPanel());
		arrowPanel.add(UIBuilder.buildToggleButton().image("up_arrow.gif").preferredSize(20, 20)
				.toolTip("Move view up.").action("PAN_UP", getController()).create());
		arrowPanel.add(new JPanel());
		arrowPanel.add(UIBuilder.buildToggleButton().image("left_arrow.gif").preferredSize(20, 20)
				.toolTip("Move view left.").action("PAN_LEFT", getController()).create());
		arrowPanel.add(new JPanel());
		arrowPanel.add(UIBuilder.buildToggleButton().image("right_arrow.gif").preferredSize(20, 20)
				.toolTip("Move view right.").action("PAN_RIGHT", getController()).create());
		arrowPanel.add(new JPanel());
		arrowPanel.add(UIBuilder.buildToggleButton().image("down_arrow.gif").preferredSize(20, 20)
				.toolTip("Move view down.").action("PAN_DOWN", getController()).create());
		arrowPanel.add(new JPanel());
		arrowPanel.setBorder(BorderFactory.createBevelBorder(NORMAL));
		Image gridImage = UIBuilder.getImage("grid.gif");
		JToggleButton tglGrid = (gridImage == null) ? new JToggleButton("Grid")
				: new JToggleButton(new ImageIcon(gridImage));
		tglGrid.setActionCommand("TOGGLE_GRID");
		tglGrid.addActionListener(getController());

		playToolBar.add(playBttn);
		playToolBar.add(stopBttn);
		playToolBar.add(rewindBttn);
		playToolBar.addSeparator();
		playToolBar.add(zoomSlider);
		playToolBar.add(arrowPanel);
		playToolBar.add(tglGrid);

		JMenu fileMenu = new JMenu("File");
		fileMenu.setPreferredSize(new Dimension(80, 30));
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

		JMenu feedbackMenu = new JMenu("Feedback");
		feedbackMenu.setMnemonic(KeyEvent.VK_B);
		feedbackMenu.setPreferredSize(new Dimension(80, 30));
		feedbackMenu.add(UIBuilder.makeMenuItem("Last Results",
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK), KeyEvent.VK_R, getController()));
		feedbackMenu.add(UIBuilder.makeMenuItem("Statistics",
				KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK), KeyEvent.VK_T, getController()));
		feedbackMenu.add(UIBuilder.makeMenuItem("Upload", KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK),
				KeyEvent.VK_U, getController()));

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(feedbackMenu);

		// pane.add(menuBar, BorderLayout.PAGE_START);
		pane.add(view, BorderLayout.CENTER);
		pane.add(playToolBar, BorderLayout.PAGE_END);
		this.setJMenuBar(menuBar);
	}


	@Override
	protected void setDefaultLayout() {
		this.setSize(1024, 768);
		this.setLocationRelativeTo(null);
		Image img = UIBuilder.getImage("GamePlayScreen_background.jpg");

		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);

			this.setContentPane(new JLabel(new ImageIcon(img)));
		}
		this.setUndecorated(false);
		this.setTitle("Play your world...");

	}

}
