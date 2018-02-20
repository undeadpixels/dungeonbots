package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.BooleanScript;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;
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
	 * The script that should be executed when a bot enters. TODO: implement
	 * this functionality.
	 */
	private UserScript onEnter;

	/**
	 * Whether the Tile is solid or not. The 'isSolid' variable is set, be
	 * default, to match that of the TileType, but it can be changed.
	 */
	private boolean isSolid = false;

	/**
	 * Lazily-loaded LuaValue representing this tile
	 */
	private transient LuaValue luaValue;

	/**
	 * The type of this tile
	 */
	private TileType type;

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
		super(world, tileType.getName(), tileType.getTexture(), x, y);
		onEnter = new UserScript("onEnter", "--Do nothing.", SecurityLevel.AUTHOR);
		setType(tileType);
	}

	@Override
	public float getZ() {
		return 0;
	}

	@Override
	public boolean isSolid() {
		return isSolid;
	}

	public void setIsSolid(boolean value) {
		isSolid = value;
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
		this.isSolid = tileType.isSolid();
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
		this.sprite.setTexture(type.getTexture(l, r, u, d));
	}
}
