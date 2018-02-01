package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import org.luaj.vm2.LuaValue;

/**
 * A tile in the terrain
 * 
 * NOTE - this might eventually have its hierarchy changed, as I'm not sure it needs to extend Entity.
 * If I get around to thinking about it more, I'll make some kind of github issue
 */
public class Tile extends SpriteEntity {

	/**
	 * Lazily-loaded LuaValue representing this tile
	 */
	private LuaValue luaValue;
	
	/**
	 * True if this tile cannot be walked through
	 */
	private boolean solid;

	/**
	 * @param world		The world to contain this Actor
	 * @param name		The name of this tile
	 * @param tex		A texture for this Actor
	 * @param x			Location X, in tiles
	 * @param y			Location Y, in tiles
	 * @param solid	True, if this tile cannot be walked through
	 */
	public Tile(World world, String name, TextureRegion tex, float x, float y, boolean solid) {
		super(world, name, tex, x, y);
		this.solid = solid;
	}

	@Override
	public float getZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSolid() {
		return solid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public LuaValue getLuaValue() {
		if(this.luaValue == null)
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		return this.luaValue;
	}

	@Override
	public int getId() {
		return id;
	}

	@Bind
	@Deprecated
	public static LuaValue Wall(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile(
				(World)lworld.checktable().checkuserdata(World.class),
				"wall",
				null,
				lx.tofloat(),
				ly.tofloat(),
				true);
		return LuaProxyFactory.getLuaValue(t);
	}

	@Bind
	@Deprecated
	public static LuaValue Floor(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile(
				(World)lworld.checktable().checkuserdata(World.class),
				"floor",
				null,
				lx.tofloat(),
				ly.tofloat(),
				false);
		return LuaProxyFactory.getLuaValue(t);
	}

	@Bind
	@Deprecated
	public static LuaValue Goal(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile(
				(World)lworld.checktable().checkuserdata(World.class),
				"goal",
				null,
				lx.tofloat(),
				ly.tofloat(),
				false);
		return LuaProxyFactory.getLuaValue(t);
	}
}
