package com.undead_pixels.dungeon_bots.desktop;

import javax.swing.JFrame;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.undead_pixels.dungeon_bots.DungeonBotsMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		// TODO - construct JFrame
		JFrame frame = new JFrame("DungeonBots");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO - deleteme and replace with some kind of listener for saving
		
		LwjglAWTCanvas canvas = new LwjglAWTCanvas(DungeonBotsMain.instance);
		frame.getContentPane().add(canvas.getCanvas());
		frame.pack();
		frame.setVisible(true);
		frame.setSize(1024, 768);
		
		//LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//new LwjglApplication(DungeonBotsMain.instance, config);
	}
}
 