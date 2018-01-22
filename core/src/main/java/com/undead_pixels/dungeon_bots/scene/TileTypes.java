package com.undead_pixels.dungeon_bots.scene;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TileTypes {

	
	public class TileType {
		public TextureRegion textureRegion;

		public TileType(TextureRegion textureRegion) {
			super();
			this.textureRegion = textureRegion;
			// TODO - make the textureRegion dependent on surrounding tiles (so walls will flow nicely together and such)
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
