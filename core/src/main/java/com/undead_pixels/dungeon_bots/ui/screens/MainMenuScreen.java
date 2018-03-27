package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;
import com.undead_pixels.dungeon_bots.ui.WindowListenerAdapter;

/**
 * The menu where users select Play, Create, or Community
 */
public class MainMenuScreen extends Screen {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public MainMenuScreen() {
		super();
	}


	@Override
	protected ScreenController makeController() {
		return new ScreenController() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getActionCommand()) {
				case "PLAY":
					/* LevelPack levelPack = new LevelPack("My Level Pack",
					 * DungeonBotsMain.instance.getUser(), new World(new
					 * File("default.lua")));
					 * 
					 * if (levelPack.getCurrentPlayer() != null &&
					 * !levelPack.getCurrentPlayer().equals(DungeonBotsMain.
					 * instance.getUser())) { throw new
					 * RuntimeException("Cannot switch to a game being played by another player."
					 * ); } */
					LevelPackScreen lps = LevelPackScreen.fromDirectory(System.getProperty("user.dir"));
					DungeonBotsMain.instance.setCurrentScreen(lps);
					// lps.setSelection(lps.getLevelPackAt(0), 0);

					break;
				case "CREATE":
					levelPack = new LevelPack("My Level Pack", DungeonBotsMain.instance.getUser(),
							new World(new File("blank.lua")));
					DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen(levelPack));
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


		};
	}


	@Override
	protected void addComponents(Container pane) {
		JLabel lblTitle = UIBuilder.buildLabel().text("DungeonBots").alignmentX(CENTER_ALIGNMENT).create();
		try {
			Font font = UIBuilder.getFont("DawnLike/GUI/SDS_8x8.ttf").deriveFont(36f);
			lblTitle.setFont(font);
		} catch (Exception ex) {

		}

		JButton bttnPlay = UIBuilder.buildButton().toolTip("Start a game as a player.").text("Play")
				.action("PLAY", getController()).hotkey(KeyEvent.VK_P).margin(10, 10, 10, 10)
				.alignmentX(CENTER_ALIGNMENT).create();

		bttnPlay.requestFocus();

		JButton bttnCreate = UIBuilder.buildButton().toolTip("Edit a game as an author.").text("Create")
				.action("CREATE", getController()).hotkey(KeyEvent.VK_C).margin(10, 10, 10, 10)
				.alignmentX(CENTER_ALIGNMENT).create();

		JButton bttnCommunity = UIBuilder.buildButton().toolTip("Go to the online community.").text("Community")
				.action("COMMUNITY", getController()).hotkey(KeyEvent.VK_U).margin(10, 10, 10, 10)
				.alignmentX(CENTER_ALIGNMENT).create();

		JButton bttnQuit = UIBuilder.buildButton().toolTip("Quit the game.").text("Quit")
				.action("QUIT", getController()).hotkey(KeyEvent.VK_Q).margin(5, 5, 5, 5).alignmentX(CENTER_ALIGNMENT)
				.create();

		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(lblTitle);
		buttonPanel.add(Box.createVerticalStrut(50));
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
		Image img = UIBuilder.getImage("images/dungeon_room.jpg");

		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);

			this.setContentPane(new JLabel(new ImageIcon(img)));
		}

		this.setUndecorated(true);

	}

}
