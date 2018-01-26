package com.undead_pixels.dungeon_bots.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.undead_pixels.dungeon_bots.scene.TileTypes;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;

/**
 * The screen for the regular game
 */
public class GameView extends GDXandSwingScreen {
	private Stage stage = new Stage(); // deleting this somehow makes it not work...?

	Texture img = new Texture("badlogic.jpg");
	SpriteBatch batch;
	
	OrthographicCamera cam;
	private boolean didInitCam = false;
	
	private World world;
	
	public GameView() {
		world = new World();
		
		// XXX - testing code; remove and replace with lua
		world.setSize(16, 16);

		int tilesize = 16;
		TileTypes tt = new TileTypes();

		// TODO - visually test these all at some point
		Vector2[] offsetsWalls = new Vector2[] {
				new Vector2(1, 1), // 0 default
				new Vector2(1, 0), // 1 only left
				new Vector2(1, 0), // 2 only right
				new Vector2(1, 0), // 3 only left+right
				new Vector2(0, 1), // 4 only up
				new Vector2(2, 2), // 5 only up+left
				new Vector2(0, 2), // 6 only up+right
				new Vector2(4, 2), // 7 no down
				new Vector2(0, 1), // 8 only down
				new Vector2(2, 0), // 9 only down+left
				new Vector2(0, 0), // A only down+right
				new Vector2(4, 0), // B no up
				new Vector2(0, 1), // C only down+up
				new Vector2(5, 1), // D no right
				new Vector2(3, 1), // E no left
				new Vector2(4, 1), // F all
		};
		Vector2[] offsetsFloors = new Vector2[] {
				new Vector2(5, 0), // 0 default
				new Vector2(6, 1), // 1 only left
				new Vector2(4, 1), // 2 only right
				new Vector2(5, 1), // 3 only left+right
				new Vector2(3, 2), // 4 only up
				new Vector2(2, 2), // 5 only up+left
				new Vector2(0, 2), // 6 only up+right
				new Vector2(1, 2), // 7 no down
				new Vector2(3, 0), // 8 only down
				new Vector2(2, 0), // 9 only down+left
				new Vector2(0, 0), // A only down+right
				new Vector2(1, 0), // B no up
				new Vector2(3, 1), // C only down+up
				new Vector2(2, 1), // D no right
				new Vector2(0, 1), // E no left
				new Vector2(1, 1), // F all
		};
		
		
		tt.registerTile("floor", new Texture("DawnLike/Objects/Floor.png"), 16, 0, 3, offsetsFloors, false);
		tt.registerTile("wall", new Texture("DawnLike/Objects/Wall.png"), 16, 0, 3, offsetsWalls, false);
		
		for(int i = 0; i < 16; i++) {
			for(int j = 0; j < 16; j++) {
				if(i != 0 && j != 0 && i != 15 && j != 15) {
					world.setTile(j, i, tt.getTile("floor"));
				} else {
					world.setTile(j, i, tt.getTile("wall"));
				}
			}
		}

		world.setTile(5, 5, tt.getTile("wall"));
		world.setTile(4, 5, tt.getTile("wall"));
		world.setTile(6, 5, tt.getTile("wall"));
		world.setTile(5, 4, tt.getTile("wall"));
		world.setTile(5, 6, tt.getTile("wall"));
		
		TextureRegion tr = new TextureRegion(new Texture("DawnLike/Characters/Player0.png"), tilesize*0, tilesize*0, tilesize, tilesize);
		
		Actor a = new Actor(world, "asdf", tr);
		world.addEntity(a);
		a.moveInstantly(Actor.Direction.UP, 6);
		a.moveInstantly(Actor.Direction.RIGHT, 6);
		
		//Gdx.input.setInputProcessor(stage);
	}
	
	public void resize(int w, int h) {
		// TODO - we need this somehow
		//stage.getViewport().update(w, h, true);
		//batch.setProjectionMatrix(stage.getCamera().projection);
		//batch.setTransformMatrix(stage.getCamera().view);
		//OrthographicCamera cam = new OrthographicCamera(w, h);
		//cam.translate(w/2, h/2);
		//cam.update();
		//batch.setProjectionMatrix(cam.projection);
		//batch.setTransformMatrix(cam.view);
	}
		
	
	public void render(float dt) {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		if(!didInitCam) {
			batch = new SpriteBatch();
			cam = new OrthographicCamera(w, h);
			
			float ratioW = w / world.getSize().x;
			float ratioH = h / world.getSize().y;
			if(ratioW < ratioH) {
				cam.zoom = 1.0f / ratioW;
			} else {
				cam.zoom = 1.0f / ratioH;
			}
    			cam.position.x = world.getSize().x - .5f;
    			cam.position.y = world.getSize().y - .5f;
    			didInitCam = true;
		}

		cam.viewportWidth = w;
		cam.viewportHeight = h;
		
		cam.update();
		batch.setProjectionMatrix(cam.projection);
		batch.setTransformMatrix(cam.view);
		
		if(world != null) {
			world.update(dt);
			world.render(batch);
		}

		//Gdx.gl.glClearColor(1, 0, 0, 1);
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//batch.begin();
		//batch.draw(img, -img.getWidth()/2, -img.getHeight()/2);
		//batch.end();
	}
}
