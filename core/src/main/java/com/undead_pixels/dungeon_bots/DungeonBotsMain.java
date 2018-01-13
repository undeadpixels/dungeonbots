package com.undead_pixels.dungeon_bots;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * The main class. Basically, all it does is point to the screen that we are actually trying to render.
 *
 */
public class DungeonBotsMain extends Game {
	
	/**
	 * Singleton instance
	 */
	public static final DungeonBotsMain instance = new DungeonBotsMain();
	
	/**
	 * private constructor for singleton
	 */
	private DungeonBotsMain() {
		
	}
	
	
	
	

	@Override
	public void create() {
		setScreen(new NullScreen());
	}
	
	
	
	
	
	
	
	
	/**
	 * This will be deleted eventually, but it at least allows us to have a fake screen
	 */
	public static class NullScreen extends ScreenAdapter {

		SpriteBatch batch = new SpriteBatch();
		Texture img = new Texture("badlogic.jpg");
		

		@Override
		public void render (float delta) {
			Gdx.gl.glClearColor(1, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			batch.draw(img, 0, 0);
			batch.end();
		}

		@Override
		public void dispose () {
			batch.dispose();
			img.dispose();
		}
		
	}
}
