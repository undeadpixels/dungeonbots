package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.World.MessageListener;
import com.undead_pixels.dungeon_bots.scene.entities.Bot;
import com.undead_pixels.dungeon_bots.scene.entities.HasImage;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.ui.JEntityEditor;
import com.undead_pixels.dungeon_bots.ui.JExponentialSlider;
import com.undead_pixels.dungeon_bots.ui.JSemitransparentPanel;
import com.undead_pixels.dungeon_bots.ui.JMessagePane;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.WorldView;
import com.undead_pixels.dungeon_bots.utils.StringWrap;

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
	private static final String COMMAND_CENTER_VIEW = "CENTER_VIEW";

	/** The JComponent that views the current world state. */
	private WorldView view;
	private final boolean isSwitched;
	private JMessagePane _MessagePane = JMessagePane.create();;
	private final World originalWorld;
	private AbstractButton _PlayStopBttn;
	private Tool.ViewControl _ViewControl;
	private LinkedList<Poptart> poptartQueue = new LinkedList<>();


	public static class Poptart {

		public final HasImage img;
		public final String title, message;


		public Poptart(HasImage img, String title, String message) {
			super();
			this.img = img;
			this.title = title;
			this.message = message;
		}

	}


	public GameplayScreen(LevelPack pack, boolean switched) {
		super(pack);
		this.isSwitched = switched;
		this.originalWorld = world;
		this.setWorld(Serializer.deepCopy(originalWorld));
	}


	/**
	 * @param world - the new world to use
	 */
	private void setWorld (World world) {
		this.world.registerMessageListener(null); // un-listen to the old world
		
		this.world = world;
		
		world.registerMessageListener(new MessageListener() {

			@Override
			public void message(HasImage src, String message, LoggingLevel level) {
				GameplayScreen.this.message(src, message, level);
			}
		});
		world.registerPoptartListener((p) -> enqueuePoptart(p));
		
		if(view != null) {
			view.setWorld(world);
		}
		
		world.onBecomingVisibleInGameplayButNotFromTheLevelEditor();
		
		Controller controller = (Controller) getController();
		if(controller != null) {
			Tool.Selector selector = controller.selector;
			if(selector != null) {
				selector.setWorld(world);
			}
		}
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
			}, true);
		} else {
			view = new WorldView(world, (w) -> {
				//new Thread(() -> {
					JOptionPane.showMessageDialog(this,
							w.getState().entrySet().stream()
									.map(item -> item.getKey() + " : " + item.getValue().toString())
									.reduce((a,b) -> a + "\n" + b)
									.orElse(""),
							"You Win",
							JOptionPane.PLAIN_MESSAGE);

					final int index = levelPack.getCurrentWorldIndex();
					final int count = levelPack.getAllWorlds().length;
					if(index < count - 1) {
						levelPack.setCurrentWorld(index + 1);
						DungeonBotsMain.instance.setCurrentScreen(new GameplayScreen(levelPack, false));
					}
					else {
						DungeonBotsMain.instance.setCurrentScreen(LevelPackScreen.fromDirectory(System.getProperty("user.dir")));
					}
					//DungeonBotsMain.instance.setCurrentScreen(new ResultsScreen(levelPack, w));
				//}).start();
			}, true);
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

		/*
		JSlider zoomSlider = new JSlider();
		zoomSlider.setName("zoomSlider");
		zoomSlider.addChangeListener((ChangeListener) getController());
		zoomSlider.setBorder(BorderFactory.createTitledBorder("Zoom"));
		*/
		JExponentialSlider speedSlider = new JExponentialSlider(WorldView.MIN_SPEED, WorldView.MAX_SPEED, view.speed);
		speedSlider.setName("speedSlider");
		speedSlider.addChangeListener((ChangeListener) getController());
		speedSlider.setBorder(BorderFactory.createTitledBorder("Speed"));

		// Layout the toolbar at the bottom of the screen for game stop/start
		// and for view control.
		if(isSwitched) {
			JButton switchBttn = UIBuilder.buildButton().image("icons/arrow_switch.png").text("Return to Editor")
					.action("Switch to Editor", getController())
					.border(new EmptyBorder(10, 10, 10, 10)).create();
			playToolBar.add(switchBttn);
		} else {
			playToolBar.add(UIBuilder.buildButton().image("icons/turn off.png").toolTip("Go back to start menu.")
					.action(COMMAND_MAIN_MENU, getController()).text("Exit").border(new EmptyBorder(10, 10, 10, 10))
					.create());
		}
		playToolBar.addSeparator();
		playToolBar.add(UIBuilder.buildButton().image("icons/rewind.png").toolTip("Rewind the game.")
				.action(COMMAND_REWIND, getController()).preferredSize(50, 50).border(new EmptyBorder(10, 10, 10, 10))
				.create());
		playToolBar.add(_PlayStopBttn = UIBuilder.buildButton().image("icons/play.png").toolTip("Start the game.")
				.action(COMMAND_PLAY_STOP, getController()).preferredSize(50, 50)
				.border(new EmptyBorder(10, 10, 10, 10)).create());
		playToolBar.addSeparator();
		playToolBar.add(UIBuilder.buildButton().image("icons/save.png").toolTip("Save the game state.")
				.action(COMMAND_SAVE, getController()).text("Save").border(new EmptyBorder(10, 10, 10, 10)).create());
		//playToolBar.add(zoomSlider);
		playToolBar.add(speedSlider);
		playToolBar
				.add(UIBuilder.buildButton().image("icons/zoom.png").text("Center view").toolTip("Set view to center.")
						.action(COMMAND_CENTER_VIEW, getController()).border(new EmptyBorder(10, 10, 10, 10)).create());
		playToolBar.add(
				UIBuilder.buildToggleButton().image("images/grid.jpg").text("Grid lines").toolTip("Turn grid off/on.")
						.border(new EmptyBorder(10, 10, 10, 10)).action(COMMAND_TOGGLE_GRID, getController()).create());


		SwingUtilities.invokeLater(new Runnable() {

			// Must be invoked later because the GamePlayScreen's isSwitched
			// property hasn't been set when addComponents is called.
			@Override
			public void run() {
			}

		});

		// Create the message pane
		Image emblemImg = levelPack.getLevelEmblem(levelPack.getLevelIndex()).getScaledInstance(250, 100,
				Image.SCALE_DEFAULT);
		JLabel emblem = new JLabel(new ImageIcon(emblemImg));
		emblem.setLayout(new BorderLayout());
		emblem.setPreferredSize(new Dimension(250, 100));
		_MessagePane.setFocusable(false);
		_MessagePane.setPreferredSize(new Dimension(250, -1));
		// TODO: consult http://java-sl.com/wrap.html for forced wrap of long
		// lines
		JScrollPane messageScroller = new JScrollPane(_MessagePane);
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		messagePanel.add(emblem, BorderLayout.NORTH);
		messagePanel.add(messageScroller, BorderLayout.CENTER);


		pane.add(view, BorderLayout.CENTER);
		pane.add(playToolBar, BorderLayout.PAGE_END);
		pane.add(messagePanel, BorderLayout.LINE_END);

		view.registerUpdateListener(() -> updatePoptart());

	}


	private static final int POPTART_WIDTH = 500;
	private static final int POPTART_HEIGHT = 200;
	JSemitransparentPanel semitrans = new JSemitransparentPanel();


	/**
	 * @return
	 */
	private void updatePoptart() {
		if (poptartIsUp) {


			semitrans.setLocation(view.getWidth() / 2 - POPTART_WIDTH / 2,
					view.getHeight() + view.getLocation().y - POPTART_HEIGHT - 40);
		}
	}


	boolean poptartIsUp = false;


	private void enqueuePoptart(Poptart p) {
		poptartQueue.add(p);

		presentPoptart();
	}


	private void presentPoptart() {
		if (poptartQueue.isEmpty())
			return;
		if (poptartIsUp)
			return;

		int padding = 32;

		poptartIsUp = true;
		Poptart p = poptartQueue.pop();

		semitrans.setFloatingFlavor(JSemitransparentPanel.FloatingFlavor.ANCHORED);
		semitrans.setAnchor(0.0f, 0.0f);
		semitrans.setSize(POPTART_WIDTH, POPTART_HEIGHT);
		// ImageIcon terminalImage = new ImageIcon("icons/terminal.png");
		// JLabel label = new JLabel(terminalImage);

		Box popPane = new Box(BoxLayout.Y_AXIS);
		ImageIcon img = new ImageIcon(p.img.getImage().getScaledInstance(50, 50, Image.SCALE_FAST));

		Box headBox = new Box(BoxLayout.X_AXIS);
		Box msgBox = new Box(BoxLayout.X_AXIS);

		JLabel titleLabel = new JLabel(p.title, img, JLabel.LEADING);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD).deriveFont(24.0f));
		headBox.add(titleLabel);
		headBox.add(Box.createGlue());
		String msgWrapped = StringWrap.wrap(p.message, 60, 3);
		System.out.println(msgWrapped);
		JLabel msgLabel = new JLabel(msgWrapped);
		msgLabel.setMaximumSize(new Dimension(POPTART_WIDTH - padding, 64));
		msgBox.add(msgLabel);
		msgBox.add(Box.createGlue());
		popPane.add(Box.createGlue());

		Box okBox = new Box(BoxLayout.X_AXIS);
		okBox.add(Box.createGlue());

		JButton okButton = new JButton("Ok");
		okBox.add(okButton);

		okButton.addActionListener((e) -> {
			poptartIsUp = false;
			semitrans.getContentPane().removeAll();
			this.getLayeredPane().remove(semitrans);

			presentPoptart();
		});


		popPane.add(headBox);
		popPane.add(msgBox);
		popPane.add(Box.createGlue());
		popPane.add(okBox);

		semitrans.getContentPane().add(popPane);
		semitrans.setHasAnchorTail(false);
		popPane.setPreferredSize(new Dimension(POPTART_WIDTH - padding, POPTART_HEIGHT - padding));
		semitrans.recursiveTransparentify();
		this.getLayeredPane().add(semitrans, (Integer) 100);

		okButton.requestFocusInWindow();
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
	public void message(String text, LoggingLevel level) {
		_MessagePane.message(text, level.color, level);
	}


	/**Posts the given message to the message pane, with the given sender's icon.*/
	public void message(HasImage sender, String text, LoggingLevel level) {
		this.message(sender, text, level, level.color);
	}


	/**Posts the given message to the message pane, with the given sender's icon.*/
	public void message(HasImage sender, String text, LoggingLevel level, Color color) {
		_MessagePane.message(sender, text, color, level);
	}


	/**Posts the given image to the message pane.*/
	public void message(Image image, int width, int height) {
		_MessagePane.message(image, width, height);
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
		private File saveFile = null;


		/** Called when the zoom slider's state changes. */
		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() instanceof JSlider) {
				JSlider sldr = (JSlider) e.getSource();
				if (sldr.getName().equals("zoomSlider")) {
					OrthographicCamera cam = view.getCamera();
					if (cam != null) {
						_ViewControl.setZoomAsPercentage((float) sldr.getValue() / sldr.getMaximum());
					}
				} else if (sldr.getName().equals("speedSlider")){
					JExponentialSlider jes = (JExponentialSlider)sldr;
					view.speed = (float)jes.getExpValue();
				}
			}
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {

			case COMMAND_CENTER_VIEW:
				// Point2D.Float worldSize = world.getSize();
				// Point2D.Float center = new Point2D.Float(worldSize.x / 2,
				// worldSize.y / 2);
				// _ViewControl.setCenter(center);
				// _ViewControl.setZoomAsPercentage(0.5f);
				_ViewControl.setMapView();
				break;
			case COMMAND_MAIN_MENU:
				if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", "Main Menu",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());

				break;
			case COMMAND_QUIT:
				if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", "Quit",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					System.exit(0);
				break;

			case COMMAND_REWIND:
				World oldWorld = world;
				_MessagePane.reset();
				setWorld(Serializer.deepCopy(originalWorld));
				world.resetFrom(oldWorld);
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
				if (saveFile == null) {
					saveFile = FileControl.saveAsDialog(GameplayScreen.this, null);
				}
				if (saveFile == null) {
					System.out.println("Save cancelled.");
				} else if (LevelPackScreen.save(GameplayScreen.this.levelPack, saveFile)) {
					System.out.println("Save successful.");
				} else {
					System.err.println("Save failed.");
				}
				return;

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
