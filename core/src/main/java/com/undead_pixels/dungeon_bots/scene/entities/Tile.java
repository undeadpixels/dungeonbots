package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
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
	 * Lazily-loaded LuaValue representing this tile
	 */
	private transient LuaValue luaValue;

	/**
	 * The type of this tile
	 */
	private TileType type;

	/**
	 * @param world		The world that contains this tile
	 * @param tileType	The (initial) type of tile
	 * @param x			Location X, in tiles
	 * @param y			Location Y, in tiles
	 */
	public Tile(World world, TileType tileType, int x, int y) {
		super(world, tileType.getName(), tileType.getTexture(), x, y);
		this.type = tileType;
		// TODO Auto-generated constructor stub
	}

	@Override
	public float getZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSolid() {
		return type.isSolid();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public LuaValue getLuaValue() {
		if (this.luaValue == null)
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		return this.luaValue;
	}

	@Override
	public int getId() {
		return id;
	}

	/**
	 * Used exclusively for creating an exemplar tile to put in the Level
	 * Editor's palette.
	 */
	public static Tile worldlessTile(String name, boolean solid) {
		return null;
	}

	/*
	@Bind
	@Deprecated
	public static LuaValue Wall(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile((World) lworld.checktable().checkuserdata(World.class), "wall", null, lx.tofloat(),
				ly.tofloat(), true);
		return LuaProxyFactory.getLuaValue(t);
	}

	@Bind
	@Deprecated
	public static LuaValue Floor(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile((World) lworld.checktable().checkuserdata(World.class), "floor", null, lx.tofloat(),
				ly.tofloat(), false);
		return LuaProxyFactory.getLuaValue(t);
	}

	@Bind
	@Deprecated
	public static LuaValue Goal(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile((World) lworld.checktable().checkuserdata(World.class), "goal", null, lx.tofloat(),
				ly.tofloat(), false);
		return LuaProxyFactory.getLuaValue(t);
	}
	*/

	public void setType(TileType tileType) {
		this.type = tileType;
	}

	public TileType getType() {
		return type;
	}

	public void updateTexture(Tile l, Tile r, Tile u, Tile d) {
		this.sprite.setTexture(type.getTexture(l, r, u, d));
	}
}
