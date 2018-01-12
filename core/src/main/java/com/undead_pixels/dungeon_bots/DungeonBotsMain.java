package com.undead_pixels.dungeon_bots;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DungeonBotsMain extends Game {
	
	public static final DungeonBotsMain instance = new DungeonBotsMain();
	
	private DungeonBotsMain() {
		
	}
	
	
	
	

	@Override
	public void create() {
		setScreen(new NullScreen());
	}
	
	
	
	
	
	
	
	
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
