package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.stream.Stream;

public class RpgActor extends Actor implements GetLuaFacade, GetLuaSandbox {
	private final int STAT_COUNT = 4;
	protected final int[] stats = new int[STAT_COUNT];

	public RpgActor(World world, String name, TextureRegion tex) {
		super(world, name, tex);
	}

	public RpgActor(World world, String name, LuaSandbox script, TextureRegion tex) {
		super(world, name, script, tex);
	}

	@Bind @BindTo("stats")
	public Varargs getStats() {
		return LuaValue.varargsOf((LuaValue[])Stream.of(stats).map(CoerceJavaToLua::coerce).toArray());
	}

	@Bind @BindTo("strength")
	public int getStrength() {
		return stats[0];
	}
}
