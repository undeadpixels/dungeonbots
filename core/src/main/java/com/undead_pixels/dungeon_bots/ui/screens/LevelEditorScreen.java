/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.KeyStroke;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.file.editor.TileRegionSection;
import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.ui.WorldView;
import com.undead_pixels.dungeon_bots.ui.screens.Screen.ScreenController;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

/**
 * The screen for the level editor
 * 
 * @author Wesley
 *
 */
public class LevelEditorScreen extends Screen {

	/**
	 * The view
	 */
	private WorldView view;

	/**
	 * Current state. Used to update the world and write to file.
	 */
	private GameEditorState state;

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

		// Add the world at the bottom layer.
		view = new WorldView(DungeonBotsMain.instance.getWorld());
		view.addMouseListener(getController());
		view.setBounds(0, 0, this.getSize().width, this.getSize().height);
		view.setOpaque(false);

		// Create the palette.
		JList<Entity> paletteSelector = new JList<Entity>(DungeonBotsMain.instance.getEntityPalette());
		JScrollPane paletteScroller = new JScrollPane(paletteSelector);

		// Create the top-of-screen menu
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

		JMenu worldMenu = new JMenu("World");
		worldMenu.setMnemonic(KeyEvent.VK_B);
		worldMenu.setPreferredSize(new Dimension(80, 30));
		worldMenu.add(UIBuilder.makeMenuItem("Data", KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK),
				KeyEvent.VK_D, getController()));

		JMenu publishMenu = new JMenu("Publish");
		publishMenu.setMnemonic(KeyEvent.VK_P);
		publishMenu.setPreferredSize(new Dimension(80, 30));
		publishMenu.add(UIBuilder.makeMenuItem("Audience", KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK),
				KeyEvent.VK_A, getController()));
		publishMenu.add(UIBuilder.makeMenuItem("Upload", KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK),
				KeyEvent.VK_U, getController()));

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(worldMenu);

		pane.add(paletteScroller, BorderLayout.LINE_START);
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

	
	protected void onGameClicked(Vector2 gamePosition, int button, int clickCount, Entity gameEntity) {
		// TODO Auto-generated method stub
		
	}

	protected void onCommand(String actionCommand) {
		// TODO Auto-generated method stub
		
	}

	
}
