package com.undead_pixels.dungeon_bots.desktop;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.undead_pixels.dungeon_bots.DungeonBotsMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		JFrame frame = new JFrame("DungeonBots");
		DungeonBotsMain.instance.setFrame(frame);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO - deleteme and replace with some kind of listener for saving
		
		Container cp = frame.getContentPane();
		cp.setLayout(new BorderLayout(0, 0));
		
		LwjglAWTCanvas canvas = new LwjglAWTCanvas(DungeonBotsMain.instance);
		cp.add(canvas.getCanvas(), BorderLayout.CENTER);
		
		frame.setSize(1024, 768);
		//p.add(canvas.getCanvas());
		//canvas.getCanvas().setSize(800, 600);
		frame.setVisible(true);
		
		
		
		
		//LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//new LwjglApplication(DungeonBotsMain.instance, config);
	}
}
 