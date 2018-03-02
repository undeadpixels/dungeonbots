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
	 * The script that should be executed when a bot enters.
	 * TODO: implement this functionality.
	 */
	private UserScript onEnter;

	/**
	 * Lazily-loaded LuaValue representing this tile
	 */
	private transient LuaValue luaValue;

	/**
	 * The type of this tile
	 */
	private TileType type;
	
	private Entity occupiedBy = null;

	/**
	 * @param world
	 *            The world that contains this tile
	 * @param tileType
	 *            The (initial) type of tile
	 * @param x
	 *            Location X, in tiles
	 * @param y
	 *            Location Y, in tiles
	 */
	public Tile(World world, TileType tileType, int x, int y) {
		super(world, tileType == null ? "tile" : tileType.getName(),
				tileType == null ? null : tileType.getTexture(),
				new UserScriptCollection(),
				x, y);
onEnter = new UserScript("onEnter", "--Do nothing.", SecurityLevel.AUTHOR);
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

	/*
	 * @Bind
	 * 
	 * @Deprecated public static LuaValue Wall(LuaValue lworld, LuaValue lx,
	 * LuaValue ly) { Tile t = new Tile((World)
	 * lworld.checktable().checkuserdata(World.class), "wall", null,
	 * lx.tofloat(), ly.tofloat(), true); return LuaProxyFactory.getLuaValue(t);
	 * }
	 * 
	 * @Bind
	 * 
	 * @Deprecated public static LuaValue Floor(LuaValue lworld, LuaValue lx,
	 * LuaValue ly) { Tile t = new Tile((World)
	 * lworld.checktable().checkuserdata(World.class), "floor", null,
	 * lx.tofloat(), ly.tofloat(), false); return
	 * LuaProxyFactory.getLuaValue(t); }
	 * 
	 * @Bind
	 * 
	 * @Deprecated public static LuaValue Goal(LuaValue lworld, LuaValue lx,
	 * LuaValue ly) { Tile t = new Tile((World)
	 * lworld.checktable().checkuserdata(World.class), "goal", null,
	 * lx.tofloat(), ly.tofloat(), false); return
	 * LuaProxyFactory.getLuaValue(t); }
	 */

	/** Sets the TileType of this Tile. */
	public void setType(TileType tileType) {
		this.type = tileType;
	}

	/**
	 * Returns the TileType (which contains image display information and other
	 * default characteristics) of this tile.
	 */
	public TileType getType() {
		return type;
	}

	/** Updates the image texture of the tile based on its neighbors. */
	public void updateTexture(Tile l, Tile r, Tile u, Tile d) {
		if(type == null) {
			this.sprite.setTexture(null);
		} else {
			this.sprite.setTexture(type.getTexture(l, r, u, d));
		}
	}

	public void setOccupiedBy(Entity e) {
		occupiedBy = e;
	}
	public Entity getOccupiedBy() {
		return occupiedBy;
	}
	public boolean isOccupied() {
		return occupiedBy != null;
	}
}
