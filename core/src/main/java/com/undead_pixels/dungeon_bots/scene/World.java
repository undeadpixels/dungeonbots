package com.undead_pixels.dungeon_bots.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public class World implements Renderable {
    private LuaScript levelScript;

	private Texture backgroundImage;
	private Tile[][] tiles;
    private ArrayList<Entity> entities = new ArrayList<>();
    
    private float scale;
    private Vector2 offset = new Vector2();

    public World() {
   	 	backgroundImage = null;
   	 	tiles = new Tile[0][0];
   	 	
   	 	scale = 16.0f;
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
		SpriteBatch batch = new SpriteBatch();
		if(backgroundImage != null) {
			float w = width;
			float h = height;
			batch.draw(backgroundImage, x0, y0, w, h);
		}
		Matrix4 matrix = new Matrix4();
		matrix.translate(x0, y0, 0.0f);
		matrix.scale(scale, scale, 1.0f);
		matrix.translate(offset.x, offset.y, 0.0f);
		batch.setTransformMatrix(matrix);

		batch.begin();
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
		// TODO Auto-generated method stub
		return 0;
	}
}
