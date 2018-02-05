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
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.JDialog;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.ui.JPlayerEditor;
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
				Entity gameEntity = view.getWorld().getEntityUnderLocation(gamePosition.x, gamePosition.y);
				onGameClicked(gamePosition, e.getButton(), e.getClickCount(), gameEntity);
				e.consume();
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onCommand(e.getActionCommand());				
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
		JButton playBttn =UIBuilder.makeButton("play.jpg", "Start the game", "Play", "PLAY", getController());
		playBttn.setPreferredSize(new Dimension(50,50));
		JButton stopBttn = UIBuilder.makeButton("stop.jpg", "Stop the game", "Stop", "STOP", getController());
		stopBttn.setPreferredSize(new Dimension(50,50));
		JButton rewindBttn = UIBuilder.makeButton("rewind.jpg", "Rewind the game", "Rewind", "REWIND", getController());
		rewindBttn.setPreferredSize(new Dimension(50,50));		
		playToolBar.add(playBttn);
		playToolBar.add(stopBttn);
		playToolBar.add(rewindBttn);
		

		JMenu fileMenu = new JMenu("File");
		fileMenu.setPreferredSize(new Dimension(80,30));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(UIBuilder.makeMenuItem("Open", KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
				KeyEvent.VK_O, getController()));
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.makeMenuItem("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
				KeyEvent.VK_S, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Save As",
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK), 0, getController()));
		fileMenu.addSeparator();
		fileMenu.add(UIBuilder.makeMenuItem("Exit to Main",
				KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), KeyEvent.VK_X, getController()));
		fileMenu.add(UIBuilder.makeMenuItem("Quit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK),
				KeyEvent.VK_Q, getController()));

		JMenu feedbackMenu = new JMenu("Feedback");
		feedbackMenu.setMnemonic(KeyEvent.VK_B);
		feedbackMenu.setPreferredSize(new Dimension(80,30));
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
		this.setTitle("Create your world...");

	}

	private void onGameClicked(Vector2 position, int button, int clickCount, Entity entity) {
		
		if (_OpenEditors.containsKey(entity)){
			System.err.println("An editor is already open for this entity:  " + entity.toString());
			return;
		}

		if (entity instanceof Player) {

			JPlayerEditor jpe = new JPlayerEditor((Player) entity);
			JDialog dialog = new JDialog(this, "Player editor", Dialog.ModalityType.DOCUMENT_MODAL);
			dialog.add(jpe);
			
			dialog.pack();
			dialog.setVisible(true);
			// this.addWindowFor(jpe, "Player Editor");

		}
		System.out.println("Clicked entity " + entity);

		// System.out.println("Clicked entity "+gameEntity+" at "+ position.x+",
		// "+position.y+" (screen "+screenX+", "+screenY+")");
	}
	
	private void onCommand(String actionCommand) {
		// TODO Auto-generated method stub
		switch (actionCommand){
		case "Play":
		case "Stop":
		case "Rewind":
		case "Open":
		case "Save":
		case "Save As":
		case "Exit To Main":
		case "Quit":
		case "Last Result":
		case "Statistics":
		case "Upload":
			
			
		default:
			System.out.println("Have not implemented the command: " + actionCommand);
			break;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}
