package com.undead_pixels.dungeon_bots.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Texture;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public class World implements Renderable {
    private LuaScript levelScript;

	private Texture backgroundImage;
	private Tile[][] tiles;
    private ArrayList<Entity> entities = new ArrayList<>();
    
    
	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void render() {
		// TODO Auto-generated method stub
		
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
}
