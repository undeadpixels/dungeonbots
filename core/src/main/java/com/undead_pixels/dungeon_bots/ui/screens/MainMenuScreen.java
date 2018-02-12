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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

/**
 * The menu where users select Play, Create, or Community
 */
@SuppressWarnings("serial")
public class MainMenuScreen extends Screen {

	@Override
	protected ScreenController makeController() {
		return new ScreenController() {

			@Override
			public void actionPerformed(ActionEvent e) {
				buttonClicked(e);
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

	protected void buttonClicked(ActionEvent e) {

		switch (e.getActionCommand()) {
		case "PLAY":
			DungeonBotsMain.instance.setCurrentScreen(new GameplayScreen());
			break;
		case "CREATE":
			DungeonBotsMain.instance.setCurrentScreen(new LevelEditorScreen());
			break;
		case "COMMUNITY":
			// DungeonBotsMain.instance.setCurrentScreen(new CommunityScreen());
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
	protected void addComponents(Container pane) {

		Insets insets = new Insets(10, 10, 10, 10);
		JButton bttnPlay = UIBuilder.makeButton("play.gif", "Start a game as a player.", "PLAY", getController());
		bttnPlay.setMargin(insets);
		bttnPlay.setAlignmentX(CENTER_ALIGNMENT);
		bttnPlay.requestFocus();

		JButton bttnCreate = UIBuilder.makeButton("create.gif", "Edit a game as an author.", "CREATE", getController());
		bttnCreate.setMargin(insets);
		bttnCreate.setAlignmentX(CENTER_ALIGNMENT);
		JButton bttnCommunity = UIBuilder.makeButton("community.gif", "Go to the community.", "COMMUNITY",
				getController());
		bttnCommunity.setMargin(insets);
		bttnCommunity.setAlignmentX(CENTER_ALIGNMENT);
		JButton bttnQuit = UIBuilder.makeButton("quit.gif", "Quit the game.", "QUIT", getController());
		bttnQuit.setMargin(new Insets(5, 5, 5, 5));
		bttnQuit.setAlignmentX(CENTER_ALIGNMENT);

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
		Image img = DungeonBotsMain.getImage("dungeon_room.jpg");

		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);

			this.setContentPane(new JLabel(new ImageIcon(img)));
		}

		this.setUndecorated(true);

	}

}
