package com.undead_pixels.dungeon_bots.ui;

import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.nogdx.OrthographicCamera;
import com.undead_pixels.dungeon_bots.nogdx.SpriteBatch;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.io.File;

/**
 * The screen for the regular game
 */
public class WorldView {

	private SpriteBatch batch;
	
	private OrthographicCamera cam;
	private boolean didInitCam = false;
	
	private World world;
	
	public WorldView() {
		world = new World(new File("sample-level-packs/sample-pack-1/levels/level1.lua"));
	}
		

	public WorldView(World world) {
		AssetManager.loadAsset(AssetManager.AssetSrc.Player, Texture.class);
		AssetManager.finishLoading();
		this.world = world;
	}


	/**
	 * Updates this WorldView's world
	 * 
	 * @param dt		Delta time
	 */
	public void update(float dt) {
		if(world != null) {
			world.update(dt);
		}
	}
	
	/**
	 * Renders the world using the camera transform specific to this WorldView 
	 */
	public void render() {
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
    			cam.position.x = world.getSize().x/2;
    			cam.position.y = world.getSize().y/2;
    			didInitCam = true;
		}

		cam.viewportWidth = w;
		cam.viewportHeight = h;
		
		cam.update();
		batch.setProjectionMatrix(cam.combined);
		//batch.setTransformMatrix(cam.view);
		
		if(world != null) {
			world.render(batch);
		}
	}

	public OrthographicCamera getCamera() {
		return cam;
	}

	public Vector2 getScreenToGameCoords(int screenX, int screenY) {
		cam.update();
		Vector2 gameSpace = cam.unproject(new Vector2(screenX, screenY));
		
		return new Vector2(gameSpace.x, gameSpace.y);
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}
