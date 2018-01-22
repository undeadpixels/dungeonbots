package com.undead_pixels.dungeon_bots.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.undead_pixels.dungeon_bots.scene.World;

/**
 * The screen for the regular game
 */
public class GameView extends ScreenAdapter {
	
	private World world;
	
	public void render(float dt) {
		if(world != null) {
			world.update(dt);

			float w = Gdx.graphics.getWidth();
			float h = Gdx.graphics.getHeight();
			world.render(0.0f, 0.0f, w, h);
		}
	}
}
