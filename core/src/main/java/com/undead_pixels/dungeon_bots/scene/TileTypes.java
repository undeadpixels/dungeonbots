package com.undead_pixels.dungeon_bots.scene;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TileTypes {

	
	public class TileType {
		public TextureRegion textureRegion;

		public TileType(TextureRegion textureRegion) {
			super();
			this.textureRegion = textureRegion;
		}
	}
	
	private HashMap<String, TileType> typeMap = new HashMap<>();

	public void registerTile(String name, TextureRegion textureRegion) {
		typeMap.put(name, new TileType(textureRegion));
	}
	
	public TileType getTile(String name) {
		return typeMap.get(name);
	}
}
