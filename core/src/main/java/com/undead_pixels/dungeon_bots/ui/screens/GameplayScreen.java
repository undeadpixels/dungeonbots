package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.World.MessageListener;
import com.undead_pixels.dungeon_bots.scene.entities.HasImage;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.JMessagePane;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.WorldView;

/**
 * A screen for gameplay
 */
@SuppressWarnings("serial")
public class GameplayScreen extends Screen {

	private static final String COMMAND_PLAY_STOP = "PLAY/STOP";
	private static final String COMMAND_SAVE = "SAVE";
	private static final String COMMAND_TOGGLE_GRID = "TOGGLE_GRID";
	private static final String COMMAND_REWIND = "REWIND";
	private static final String COMMAND_MAIN_MENU = "MAIN_MENU";
	private static final String COMMAND_QUIT = "QUIT";

	/** The JComponent that views the current world state. */
	private WorldView view;
	private final boolean isSwitched;
	private JMessagePane _MessagePane;
	private final World originalWorld;
	private AbstractButton _PlayStopBttn;
	private Tool.ViewControl _ViewControl;


	public GameplayScreen(LevelPack pack, boolean switched) {
		super(pack);
		this.isSwitched = switched;
		this.originalWorld = world;
		this.world = Serializer.deepCopy(originalWorld);
		world.onBecomingVisibleInGameplay();
		world.registerMessageListener(new MessageListener() {

			@Override
			public void message(HasImage src, String message, LoggingLevel level) {
				this.message(src, message, level);
			}
		});
	}


	@Override
	protected ScreenController makeController() {
		return new Controller();
	}


	@Override
	protected void addComponents(Container pane) {

		pane.setLayout(new BorderLayout());

		// At the world at the bottom layer.
		if (this.isSwitched) {
			view = new WorldView(world, (w) -> {
				DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen(levelPack));
			});
		} else {
			view = new WorldView(world, (w) -> {
				DungeonBotsMain.instance.setCurrentScreen(new ResultsScreen(w));
			});
		}
		getController().registerSignalsFrom(view);
		view.setBounds(0, 0, this.getSize().width, this.getSize().height);
		view.setOpaque(false);
		_ViewControl = new Tool.ViewControl(view);

		// Set up the selection and view control.
		((Controller) getController()).selector = new Tool.Selector(view, GameplayScreen.this, SecurityLevel.DEFAULT)
				.setSelectsEntities(true).setSelectsTiles(false);

		// Set up the toolbar, which will be at the bottom of the screen
		JToolBar playToolBar = new JToolBar();
		playToolBar.setOpaque(false);

		JSlider zoomSlider = new JSlider();
		zoomSlider.setName("zoomSlider");
		zoomSlider.addChangeListener((ChangeListener) getController());
		zoomSlider.setBorder(BorderFactory.createTitledBorder("Zoom"));

		// Layout the toolbar at the bottom of the screen for game stop/start
		// and for view control.
		playToolBar.add(UIBuilder.buildButton().image("icons/turn off.png").toolTip("Go back to start menu.")
				.action(COMMAND_MAIN_MENU, getController()).create());
		playToolBar.addSeparator();
		playToolBar.add(_PlayStopBttn = UIBuilder.buildButton().image("icons/play.png").toolTip("Start the game.")
				.action(COMMAND_PLAY_STOP, getController()).preferredSize(50, 50).create());
		playToolBar.add(UIBuilder.buildButton().image("icons/rewind.png").toolTip("Rewind the game.")
				.action(COMMAND_REWIND, getController()).preferredSize(50, 50).create());
		playToolBar.addSeparator();
		playToolBar.add(UIBuilder.buildButton().image("icons/save.png").toolTip("Save the game state.")
				.action(COMMAND_SAVE, getController()).create());
		playToolBar.add(zoomSlider);
		playToolBar.add(UIBuilder.buildToggleButton().image("images/grid.jpg").toolTip("Turn grid off/on")
				.action(COMMAND_TOGGLE_GRID, getController()).create());


		SwingUtilities.invokeLater(new Runnable() {

			// Must be invoked later because the GamePlayScreen's isSwitched
			// property hasn't been set when addComponents is called.
			@Override
			public void run() {
				JButton switchBttn = UIBuilder.buildButton().text("Switch to Editor")
						.action("Switch to Editor", getController()).enabled(isSwitched)
						.border(BorderFactory.createRaisedSoftBevelBorder()).create();
				playToolBar.add(switchBttn);
			}
		});

		// Create the message pane
		Image emblemImg = levelPack.getLevelEmblem(levelPack.getLevelIndex()).getScaledInstance(250, 100,
				Image.SCALE_DEFAULT);
		JLabel emblem = new JLabel(new ImageIcon(emblemImg));
		emblem.setLayout(new BorderLayout());
		emblem.setPreferredSize(new Dimension(250, 100));
		_MessagePane = JMessagePane.create();
		_MessagePane.setFocusable(false);
		_MessagePane.setPreferredSize(new Dimension(250, -1));
		// TODO: consult http://java-sl.com/wrap.html for forced wrap of long
		// lines
		JScrollPane messageScroller = new JScrollPane(_MessagePane);
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		messagePanel.add(emblem, BorderLayout.PAGE_START);
		messagePanel.add(messageScroller, BorderLayout.CENTER);
		message("This is a regular message from the world.\n", Color.white);
		message("This is an error message from the world.\n", Color.red);
		message(new Player(null, "p", 0, 0), "This is a regular message from an entity.\n", Color.WHITE);
		message(new Player(null, "p", 0, 0), "This is an error message from an entity.\n", Color.RED);
		message(new Player(null, "p", 0, 0), "This is a green message.  Just because.\n", Color.green);


		pane.add(view, BorderLayout.CENTER);
		pane.add(playToolBar, BorderLayout.PAGE_END);
		pane.add(messagePanel, BorderLayout.LINE_END);


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


	/**Posts the given message to the message pane.*/
	public void message(String text) {
		message(text, Color.white);
	}


	/**Posts the given message to the message pane.*/
	public void message(String text, Color color) {
		_MessagePane.message(text, color);
	}


	/**Posts the given message to the message pane, with the given sender's icon.*/
	public void message(HasImage sender, String text, Color color) {
		_MessagePane.message(sender, text, color);
	}


	/**Posts the given image to the message pane.*/
	public void message(Image image, int width, int height) {
		_MessagePane.message(image, width, height);
	}


	/**Posts the given images to the message pane.*/
	public void message(Image[] images, int width, int height) {
		_MessagePane.message(images, width, height);
	}


	/**Updates the GUI state based on current options.*/
	private void updateGUIState() {
		if (view.getPlaying()) {
			_PlayStopBttn.setIcon(new ImageIcon("icons/abort.png"));
		} else {
			_PlayStopBttn.setIcon(new ImageIcon("icons/play.png"));
		}

	}


	private class Controller extends ScreenController
			implements MouseWheelListener, MouseInputListener, ChangeListener {

		private Tool.Selector selector = null;


		/** Called when the zoom slider's state changes. */
		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() instanceof JSlider) {
				JSlider sldr = (JSlider) e.getSource();
				if (sldr.getName().equals("zoomSlider")) {
					OrthographicCamera cam = view.getCamera();
					if (cam != null) {
						_ViewControl.setZoomAsPercentage((float)sldr.getValue() / sldr.getMaximum());
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
					LevelPack levelPack = LevelPack.fromFile(openFile.getPath());
					DungeonBotsMain.instance.setCurrentScreen(new GameplayScreen(levelPack, false));
				}

				break;
			case COMMAND_MAIN_MENU:
				if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", e.getActionCommand(),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());

				break;
			case COMMAND_QUIT:
				if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", e.getActionCommand(),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					System.exit(0);
				break;

			case COMMAND_REWIND:
				World oldWorld = world;
				world = Serializer.deepCopy(originalWorld);
				world.resetFrom(oldWorld);
				view.setWorld(world);
				world.onBecomingVisibleInGameplay();
				((Controller) getController()).selector.setWorld(world);
				break;
			case "Switch to Editor":
				DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen(levelPack));
				return;
			case COMMAND_PLAY_STOP:
				view.setPlaying(!view.getPlaying());
				updateGUIState();
				return;
			case COMMAND_TOGGLE_GRID:
				view.setShowGrid(!view.getShowGrid());
				return;
			case COMMAND_SAVE:
			case "Last Result":
			case "Statistics":
			case "Upload":

			default:
				System.out.println("GameplayScreen has not implemented the command: " + e.getActionCommand());
				break;
			}
		}


		@Override
		public void mousePressed(MouseEvent e) {
			selector.mousePressed(e);
			if (!e.isConsumed())
				_ViewControl.mousePressed(e);
		}


		@Override
		public void mouseReleased(MouseEvent e) {
			selector.mouseReleased(e);
			if (!e.isConsumed())
				_ViewControl.mouseReleased(e);
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			selector.mouseDragged(e);
			if (!e.isConsumed())
				_ViewControl.mouseDragged(e);
		}


		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			selector.mouseWheelMoved(e);
			if (!e.isConsumed())
				_ViewControl.mouseWheelMoved(e);
		}


		@Override
		public void mouseClicked(MouseEvent arg0) {

		}


		@Override
		public void mouseEntered(MouseEvent arg0) {

		}


		@Override
		public void mouseExited(MouseEvent arg0) {

		}


		@Override
		public void mouseMoved(MouseEvent arg0) {

		}


	}

}
