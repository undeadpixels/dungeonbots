package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import org.luaj.vm2.LuaValue;

/**
 * A tile in the terrain
 */
public class Tile extends SpriteEntity {

	private LuaValue luaValue;

	/**
	 * @param world		The world to contain this Actor
	 * @param name
	 * @param tex		A texture for this Actor
	 * @param x
	 * @param y
	 */
	public Tile(World world, String name, TextureRegion tex, float x, float y) {
		super(world, name, tex, x, y);
		// TODO Auto-generated constructor stub
	}

	@Override
	public float getZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSolid() {
		// TODO Auto-generated method stub
		return false;
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
	public static LuaValue Wall(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile(
				(World)lworld.checktable().checkuserdata(World.class),
				"wall",
				null,
				lx.tofloat(),
				ly.tofloat());
		return LuaProxyFactory.getLuaValue(t);
	}

	@Bind
	public static LuaValue Floor(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile(
				(World)lworld.checktable().checkuserdata(World.class),
				"floor",
				null,
				lx.tofloat(),
				ly.tofloat());
		return LuaProxyFactory.getLuaValue(t);
	}

	@Bind
	public static LuaValue Goal(LuaValue lworld, LuaValue lx, LuaValue ly) {
		Tile t = new Tile(
				(World)lworld.checktable().checkuserdata(World.class),
				"goal",
				null,
				lx.tofloat(),
				ly.tofloat());
		return LuaProxyFactory.getLuaValue(t);
	}
}
