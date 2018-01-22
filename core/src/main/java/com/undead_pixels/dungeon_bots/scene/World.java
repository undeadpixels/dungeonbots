package com.undead_pixels.dungeon_bots.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.TileTypes.TileType;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public class World implements Renderable {
    private LuaScript levelScript;

	//private Texture backgroundImage = new Texture("badlogic.jpg");
	private Texture backgroundImage;
	private Tile[][] tiles;
    private ArrayList<Entity> entities = new ArrayList<>();
    
    private Vector2 offset = new Vector2();
	SpriteBatch batch;
	
	OrthographicCamera cam;
	private boolean didInitCam = false;
    
    private int idCounter = 0;

    public World() {
   	 	backgroundImage = null;
   	 	tiles = new Tile[0][0];
    }
    // TODO - another constructor for specific resource paths
    
    
	@Override
	public void update(float dt) {
		for(Tile[] ts : tiles) {
			for(Tile t : ts) {
				t.update(dt);
			}
		}
		
		for(Entity e : entities) {
			e.update(dt);
		}
		
		// TODO - tell the levelScript that a new frame happened
	}
	
	@Override
	public void render(float x0, float y0, float width, float height) {
		System.out.println("Rendering world");

		cam.viewportWidth = width;
		cam.viewportHeight = height;
		if(!didInitCam) {
			batch = new SpriteBatch();
			cam = new OrthographicCamera(width, height);
			
			float ratioW = width / tiles.length;
			float ratioH = height / tiles[0].length;
			if(ratioW < ratioH) {
				cam.zoom = 1.0f / ratioW;
			} else {
				cam.zoom = 1.0f / ratioH;
			}
    			cam.position.x = tiles.length - .5f;
    			cam.position.y = tiles[0].length - .5f;
    			didInitCam = true;
		}
		
		//cam.translate(w/2, h/2);
		cam.update();
		batch.setProjectionMatrix(cam.projection);
		batch.setTransformMatrix(cam.view);
		
		Gdx.gl.glClearColor(.65f, .2f, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		if(backgroundImage != null) {
			float w = width;
			float h = height;
			batch.draw(backgroundImage, 0, 0);
		}

		for(Tile[] ts : tiles) {
			for(Tile t : ts) {
				t.render(batch);
			}
		}

		for(Layer layer : toLayers()) {
			for(Entity e : layer.entities) {
				e.render(batch);
			}
		}
		batch.end();
	}
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
    
	public void setSize(int w, int h) {
		// TODO - copy old tiles?
		tiles = new Tile[w][h];
	}
	
	public void setTile(int x, int y, TileType tileType) {
		// TODO - bounds checking
		// TODO - more stuff here
		Tile t = new Tile(this, "tile", null, tileType.textureRegion, (float)x, (float)y);
		
		tiles[x][y] = t;
	}
	
	private ArrayList<Layer> toLayers() {
		HashMap<Float, Layer> layers = new HashMap<>();
		
		for(Entity e : entities) {
			float z = e.getZ();
			
			Layer l = layers.get(z);
			if(l == null) {
				l = new Layer(z);
				layers.put(z, l);
			}
			
			l.add(e);
		}
		
		ArrayList<Layer> ret = new ArrayList<Layer>(layers.values());
		Collections.sort(ret);
		
		return ret;
	}
	
	private static class Layer implements Comparable<Layer> {
		private final float z;
		public Layer(float z) {
			super();
			this.z = z;
		}

		private ArrayList<Entity> entities = new ArrayList<Entity>();

		@Override
		public int compareTo(Layer o) {
			if(z == o.z) {
				return 0;
			} else if(z < o.z) {
				return -1;
			} else {
				return 1;
			}
		}
		
		public void add(Entity e) {
			entities.add(e);
		}
		
		public ArrayList<Entity> getEntities() {
			return entities;
		}
		
	}

	public int makeID() {
		return idCounter++;
	}
}
