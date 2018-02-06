package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.utils.builders.UIBuilder;

/**
 * The screen that shows users how well they did on the challenge they were given
 */
@SuppressWarnings("serial")
public class ResultsScreen extends Screen {

	public ResultsScreen(){
		super();
		this.addWindowListener(getController());
		
	}
	@Override
	protected ScreenController makeController() {
		
		return new ScreenController(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getActionCommand()){
				case "OK":
					DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());
					break;
				case "Publish":
				default:
					System.out.println("Have not implemented command: " + e.getActionCommand());
					break;
				}
				
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				System.out.println("Closed");
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing");
				DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());
				
				
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

	@Override
	protected void addComponents(Container pane) {
		
		World world = DungeonBotsMain.instance.getWorld();
		Player player = world.getPlayer();
		
		JPanel dispPanel = new JPanel();
		
		JPanel bttnPanel = new JPanel();
		bttnPanel.setLayout(new BoxLayout(bttnPanel, BoxLayout.LINE_AXIS));
		JButton bttnOK = UIBuilder.makeButton("", "Quit to Main Menu", "OK", "OK", getController());
		bttnPanel.add(bttnOK);
		bttnPanel.add(UIBuilder.makeButton("", "Publish", "Publish", "Publish", getController()));
		
		pane.setLayout(new BorderLayout());
		pane.add(dispPanel, BorderLayout.CENTER);
		pane.add(bttnPanel,  BorderLayout.PAGE_END);
		
	}

	@Override
	protected void setDefaultLayout() {
		this.setSize(640, 480);
		this.setLocationRelativeTo(null);
		//Image img = DungeonBotsMain.getImage("results_screen.jpg");
		Image img = DungeonBotsMain.getImage("dungeon_room.jpg");

		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);

			this.setContentPane(new JLabel(new ImageIcon(img)));
		}		
	}

}
