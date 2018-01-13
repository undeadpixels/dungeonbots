package com.undead_pixels.dungeon_bots.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.undead_pixels.dungeon_bots.DungeonBotsMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		// TODO - construct JFrame
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(DungeonBotsMain.instance, config);
	}
}
 