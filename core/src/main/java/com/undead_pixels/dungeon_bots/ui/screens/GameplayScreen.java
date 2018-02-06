package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.JDialog;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.FileControl;
import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.ui.JEntityEditor;
import com.undead_pixels.dungeon_bots.ui.WorldView;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

/**
 * A screen for gameplay
 */
public class GameplayScreen extends Screen {

	/** The JComponent that views the current world state. */
	private WorldView view;

	private boolean _Changed = false;

	/**
	 * On closing the screen, each open editor will also need to be closed.
	 * Additionally, it makes no sense to open an editor twice for the same
	 * Entity. That's why the open editors are tracked.
	 */
	private HashMap<Entity, JDialog> _OpenEditors = new HashMap<Entity, JDialog>();

	@Override
	protected ScreenController makeController() {

		return new ScreenController() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Vector2 gamePosition = view.getScreenToGameCoords(e.getX(), e.getY());
				Entity entity = view.getWorld().getEntityUnderLocation(gamePosition.x, gamePosition.y);

				if (_OpenEditors.containsKey(entity)) {
					System.err.println("An editor is already open for this entity:  " + entity.toString());
					return;
				}

				if (entity instanceof Player) {

					JEntityEditor jpe = new JEntityEditor((Player) entity);
					JDialog dialog = new JDialog(GameplayScreen.this, "Player editor",
							Dialog.ModalityType.DOCUMENT_MODAL);
					dialog.add(jpe);

					dialog.pack();
					dialog.setVisible(true);
					// this.addWindowFor(jpe, "Player Editor");

				}
				System.out.println("Clicked entity " + entity);

				e.consume();
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				switch (e.getActionCommand()) {
				case "Play":
				case "Stop":
				case "Rewind":
				case "Open":
					File file = FileControl.openDialog(GameplayScreen.this);
					World newWorld = new World(file);
					DungeonBotsMain.instance.setWorld(newWorld);
					break;
				case "Save":
				case "Save As":
				case "Exit to Main":
					if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", e.getActionCommand(),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());

					break;
				case "Quit":
					if (JOptionPane.showConfirmDialog(GameplayScreen.this, "Are you sure?", e.getActionCommand(),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						DungeonBotsMain.instance.setCurrentScreen(new ResultsScreen());

					break;

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
			public void mousePressed(MouseEvent arg0) {
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

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			int priorZoomSlider = 50;
			@Override
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JSlider) {
					JSlider sldr = (JSlider) e.getSource();
					if (sldr.getName().equals("zoomSlider")) {
						OrthographicCamera cam = view.getCamera();
						if (cam != null) {
							//TODO:  This is very hack-like.  Work out the math.
							int newSlider = sldr.getValue();
							cam.setZoom(cam.getZoom() + ((newSlider - priorZoomSlider) * 0.001f));
							priorZoomSlider = newSlider;
						}
					}
				}

			}

		};

	}

	@Override
	protected void addComponents(Container pane) {

		pane.setLayout(new BorderLayout());

		// At the world at the bottom layer.
		view = new WorldView(DungeonBotsMain.instance.getWorld());
		view.addMouseListener(getController());
		view.setBounds(0, 0, this.getSize().width, this.getSize().height);
		view.setOpaque(false);

		JToolBar playToolBar = new JToolBar();
		playToolBar.setOpaque(false);
		JButton playBttn = UIBuilder.makeButton("play.jpg", "Start the game", "Play", "PLAY", getController());
		playBttn.setPreferredSize(new Dimension(50, 50));
		JButton stopBttn = UIBuilder.makeButton("stop.jpg", "Stop the game", "Stop", "STOP", getController());
		stopBttn.setPreferredSize(new Dimension(50, 50));
		JButton rewindBttn = UIBuilder.makeButton("rewind.jpg", "Rewind the game", "Rewind", "REWIND", getController());
		rewindBttn.setPreferredSize(new Dimension(50, 50));
		JSlider zoomSlider = new JSlider();
		zoomSlider.setName("zoomSlider");
		zoomSlider.addChangeListener(getController());
		zoomSlider.setBorder(BorderFactory.createTitledBorder("Zoom"));
		playToolBar.add(playBttn);
		playToolBar.add(stopBttn);
		playToolBar.add(rewindBttn);
		playToolBar.addSeparator();
		playToolBar.add(zoomSlider);

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

		pane.add(menuBar, BorderLayout.PAGE_START);
		pane.add(view, BorderLayout.CENTER);
		pane.add(playToolBar, BorderLayout.PAGE_END);

	}

	@Override
	protected void setDefaultLayout() {
		this.setSize(1024, 768);
		this.setLocationRelativeTo(null);
		Image img = DungeonBotsMain.getImage("GamePlayScreen_background.jpg");

		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);

			this.setContentPane(new JLabel(new ImageIcon(img)));
		}
		this.setUndecorated(false);
		this.setTitle("Play your world...");

	}

}
