package com.undead_pixels.dungeon_bots.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;

/**
 * The screen for the regular game
 */
public class GameView extends GDXandSwingScreen {
	private Stage stage = new Stage();

	SpriteBatch batch = new SpriteBatch();
	Texture img = new Texture("badlogic.jpg");
	
	private World world;
	
	public GameView() {
		world = new World();
		
		TextureRegion tr = new TextureRegion(new Texture("badlogic.jpg"));
		
		Actor a = new Actor(world, "asdf", tr);
		world.addEntity(a);
		
		//Gdx.input.setInputProcessor(stage);
	}
	
	public void resize(int w, int h) {
		// TODO - we need this somehow
		//stage.getViewport().update(w, h, true);
		stage.getCamera().update();
		//batch.setProjectionMatrix(stage.getCamera().projection);
		//batch.setTransformMatrix(stage.getCamera().view);
		OrthographicCamera cam = new OrthographicCamera(w, h);
		//cam.translate(w/2, h/2);
		cam.update();
		//cam.setToOrtho(false, w, h);
		batch.setProjectionMatrix(cam.projection);
		batch.setTransformMatrix(cam.view);

		System.out.println("SP"+stage.getCamera().projection);
		System.out.println("SV"+stage.getCamera().view);
		System.out.println("MP"+cam.projection);
		System.out.println("MV"+cam.view);
	}
		
	
	public void render(float dt) {
		if(world != null) {
			//world.update(dt);

			float w = Gdx.graphics.getWidth();
			float h = Gdx.graphics.getHeight();
			//world.render(0.0f, 0.0f, w, h);
		}

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, -img.getWidth()/2, -img.getHeight()/2);
		batch.end();
	}
}
