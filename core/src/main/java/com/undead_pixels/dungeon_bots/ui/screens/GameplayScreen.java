package com.undead_pixels.dungeon_bots.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.ui.JPlayerEditor;
import com.undead_pixels.dungeon_bots.ui.WorldView;

public class GameplayScreen extends GDXandSwingScreen implements InputProcessor {
	private Stage stage = new Stage(); // deleting this somehow makes it not work...?
	private WorldView view;



	public GameplayScreen() {
		super();
		
		view = new WorldView();
		
		
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render(float dt) {
		view.update(dt);
		view.render();
	}
	

	@Override
	public boolean keyDown(int keycode) {
		// TODO pass these to the world to handle
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO pass these to the world to handle
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO pass these to the world to handle
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector2 gameSpace = view.getScreenToGameCoords(screenX, screenY);
		Entity e = view.getWorld().getEntityUnderLocation(gameSpace.x, gameSpace.y);
		
		if(e instanceof Player) {
			
			
			JPlayerEditor jpe = new JPlayerEditor((Player)e);
			this.addWindowFor(jpe,  "Player Editor");
			
		}

		System.out.println("Clicked entity "+e+" at "+ gameSpace.x+", "+gameSpace.y+" (screen "+screenX+", "+screenY+")");
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
