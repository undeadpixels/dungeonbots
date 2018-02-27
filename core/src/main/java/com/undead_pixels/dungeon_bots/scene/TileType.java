package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;

import java.io.Serializable;

import org.luaj.vm2.LuaValue;

/**
 * A layer of abstraction beyond regular tiles. These can be different textures depending on what's around them.
 */
public class TileType implements GetLuaFacade, Serializable {
	
	/**
	 * Possible textures
	 */
	private final TextureRegion[] textureRegions;
	
	/**
	 * If the textures should be selected from randomly
	 */
	private final boolean random;
	/**
	 * A name for this TileType
	 */
	private final String name;
	
	/**
	 * Lazily-loaded LuaValue representing this tile
	 */
	private transient LuaValue luaValue;
	
	/**
	 * True if this tile cannot be walked through
	 */
	private boolean solid;

	/**
	 * Constructor
	 * 
	 * @param textureRegions		Possible textures
	 * @param name				A name for this TileType
	 * @param random				If the textures should be selected from randomly
	 * @param solid				True if this tile cannot be walked through
	 */
	public TileType(TextureRegion[] textureRegions, String name, boolean random, boolean solid) {
		super();
		if(textureRegions.length < 16) {
			this.random = true;
		} else {
			this.random = random;
		}
		this.textureRegions = textureRegions;
		this.name = name;
		this.solid = solid;
	}

	/**
	 * Get the image texture for a tile, depending on the surrounding tiles
	 * 
	 * @param left		Tile to the left
	 * @param right		Tile to the right
	 * @param up			Tile above
	 * @param down		Tile below
	 * @return			The texture of this tile
	 */
	public TextureRegion getTexture(TileType left, TileType right, TileType up, TileType down) {
		if(random) {
			if(textureRegions.length == 1) {
				return textureRegions[0];
			} else {
				// TODO - randomize
				return textureRegions[0];
			}
		} else {
			int idx = (this == left  ? 1: 0)
					| (this == right ? 2: 0)
					| (this == up    ? 4: 0)
					| (this == down  ? 8: 0);

			return textureRegions[idx];
		}
	}

	public TextureRegion getTexture(Tile left, Tile right, Tile up, Tile down) {
		return getTexture(left == null ? null : left.getType(),
				right == null ? null : right.getType(),
				up == null ? null : up.getType(),
				down == null ? null : down.getType());
	}

	@Override
	public int getId() {
		return 0;
	}

	public String getName() {
		return name;
	}

	@Override
	public LuaValue getLuaValue() {
		if (this.luaValue == null)
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		return this.luaValue;
	}

	/**
	 * @return	True if this tile cannot be walked through
	 */
	public boolean isSolid() {
		return solid;
	}

	public TextureRegion getTexture() {
		return textureRegions[0];
	}
}
