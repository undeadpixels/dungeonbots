package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.Container;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;

/**
 * The menu where users select Play, Create, or Community
 */
@SuppressWarnings("serial")
public class MainMenuScreen extends Screen {

	public MainMenuScreen() {
		super(null);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ScreenController makeController() {
		return new ScreenController() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getActionCommand()) {
				case "PLAY":
					DungeonBotsMain.instance.setCurrentScreen(DungeonBotsMain.ScreenType.GAMEPLAY);
					break;
				case "CREATE":
					DungeonBotsMain.instance.setCurrentScreen(DungeonBotsMain.ScreenType.LEVEL_EDITOR);
					break;
				case "COMMUNITY":
					// DungeonBotsMain.instance.setCurrentScreen(new
					// CommunityScreen());
					try {
						java.awt.Desktop.getDesktop().browse(CommunityScreen.homeURI);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				case "QUIT":
					System.exit(0);
					break;
				}
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

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub

			}

		};
	}

	@Override
	protected void addComponents(Container pane) {

		JButton bttnPlay = UIBuilder.buildButton().image("play.gif").toolTip("Start a game as a player.").text("PLAY")
				.action("PLAY", getController()).hotkey(KeyEvent.VK_P).margin(10, 10, 10, 10)
				.alignmentX(CENTER_ALIGNMENT).create();
		bttnPlay.requestFocus();

		JButton bttnCreate = UIBuilder.buildButton().image("create.gif").toolTip("Edit a game as an author.")
				.text("CREATE").action("CREATE", getController()).hotkey(KeyEvent.VK_C).margin(10, 10, 10, 10)
				.alignmentX(CENTER_ALIGNMENT).create();

		JButton bttnCommunity = UIBuilder.buildButton().image("community.gif").toolTip("Go to the online community.")
				.text("COMMUNITY").action("COMMUNITY", getController()).hotkey(KeyEvent.VK_U).margin(10, 10, 10, 10)
				.alignmentX(CENTER_ALIGNMENT).create();

		JButton bttnQuit = UIBuilder.buildButton().image("quit.gif").toolTip("Quit the game.").text("QUIT")
				.action("QUIT", getController()).hotkey(KeyEvent.VK_Q).margin(5, 5, 5, 5).alignmentX(CENTER_ALIGNMENT)
				.create();

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(bttnPlay);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(bttnCreate);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(bttnCommunity);
		buttonPanel.add(Box.createVerticalStrut(30));
		buttonPanel.add(bttnQuit);
		buttonPanel.setOpaque(false);

		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.add(Box.createVerticalGlue());
		pane.add(Box.createHorizontalGlue());
		pane.add(buttonPanel);
		pane.add(Box.createVerticalGlue());
		pane.add(Box.createHorizontalGlue());

	}

	@Override
	protected void setDefaultLayout() {
		this.setSize(640, 480);
		this.setLocationRelativeTo(null);
		Image img = UIBuilder.getImage("dungeon_room.jpg");

		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);

			this.setContentPane(new JLabel(new ImageIcon(img)));
		}

		this.setUndecorated(true);

	}

}
