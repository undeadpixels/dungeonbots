package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;

import java.io.Serializable;
import java.util.Arrays;

import org.luaj.vm2.LuaValue;

/**
 * A layer of abstraction beyond regular tiles. These can be different textures depending on what's around them.
 */
public class TileType implements GetLuaFacade, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
	private String name;
	
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
			int idx = (this.equals(left)  ? 1: 0)
					| (this.equals(right) ? 2: 0)
					| (this.equals(up)    ? 4: 0)
					| (this.equals(down)  ? 8: 0);

			return textureRegions[idx];
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (random ? 1231 : 1237);
		result = prime * result + (solid ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileType other = (TileType) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (random != other.random)
			return false;
		if (solid != other.solid)
			return false;
		return true;
	}

	public TextureRegion getTexture(Tile left, Tile right, Tile up, Tile down) {
		return getTexture(left == null ? null : left.getType(),
				right == null ? null : right.getType(),
				up == null ? null : up.getType(),
				down == null ? null : down.getType());
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

	/**
	 * @return	The default texture of this TileType
	 */
	public TextureRegion getTexture() {
		return textureRegions[0];
	}
}
