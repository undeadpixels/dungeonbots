package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.BooleanScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import org.luaj.vm2.LuaValue;

/**
 * A tile in the terrain
 * 
 * NOTE - this might eventually have its hierarchy changed, as I'm not sure it
 * needs to extend Entity. If I get around to thinking about it more, I'll make
 * some kind of github issue
 */
public class Tile extends SpriteEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Lazily-loaded LuaValue representing this tile
	 */
	private transient LuaValue luaValue;

	/**
	 * The type of this tile
	 */
	private TileType type;
	
	/**
	 * The entity that is currently occupying this Tile
	 */
	private Entity occupiedBy = null;

	/**
	 * @param world		The world that contains this tile
	 * @param tileType	The (initial) type of tile
	 * @param x			Location X, in tiles
	 * @param y			Location Y, in tiles
	 */
	public Tile(World world, TileType tileType, int x, int y) {
		super(world, tileType == null ? "tile" : tileType.getName(),
				tileType == null ? null : tileType.getTexture(),
				new UserScriptCollection(),
				x, y);
		this.type = tileType;
	}

	@Override
	public float getZ() {
		return 0;
	}

	@Override
	public boolean isSolid() {
		return type != null && type.isSolid();
	}

	@Override
	public LuaValue getLuaValue() {
		if (this.luaValue == null)
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		return this.luaValue;
	}

	/**
	 * @param tileType The new type of this Tile
	 */
	public void setType(TileType tileType) {
		this.type = tileType;
	}

	/**
	 * Returns the TileType (which contains image display information and other
	 * default characteristics) of this tile.
	 * 
	 * @return	The current TileType
	 */
	public TileType getType() {
		return type;
	}

	/**
	 * Updates the texture of the tile based on its neighbors.
	 * 
	 * @param l
	 * @param r
	 * @param u
	 * @param d
	 */
	public void updateTexture(Tile l, Tile r, Tile u, Tile d) {
		if(type == null) {
			this.sprite.setTexture(null);
		} else {
			this.sprite.setTexture(type.getTexture(l, r, u, d));
		}
	}

	/**
	 * @param e	The entity now occupying this tile
	 */
	public void setOccupiedBy(Entity e) {
		occupiedBy = e;
	}
	
	/**
	 * @return	The entity currently occupying this tile
	 */
	public Entity getOccupiedBy() {
		return occupiedBy;
	}
	
	/**
	 * @return	True if something is occupying this tile
	 */
	public boolean isOccupied() {
		return occupiedBy != null;
	}
}
