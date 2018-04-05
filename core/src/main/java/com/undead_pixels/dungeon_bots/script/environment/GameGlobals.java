package com.undead_pixels.dungeon_bots.script.environment;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseIoLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseOsLib;

import java.util.stream.Stream;

/**
 * Contains static definitions of LuaJ globals for Authors and Players
 */
public class GameGlobals {

	/**
	 * Creates a new Globals chunk consisting of a Lua Jse libraries
	 * that are approved for use by the player.
	 * @return A new LuaJ globals chunk
	 */
	public static Globals playerGlobals() {
		return purify(load(
				new JseBaseLib(),
				new PackageLib(),
				new Bit32Lib(),
				new TableLib(),
				new StringLib(),
				new JseMathLib()));
	}

	/**
	 * Creates a new Globals chunk consisting of a Lua Jse libraries
	 * that are approved for use by the author.
	 * @return A new LuaJ globals chunk
	 */
	public static Globals authorGlobals() {
		return purify(load(
				new JseBaseLib(),
				new PackageLib(),
				new StringLib(),
				new JseMathLib(),
				new TableLib(),
				new Bit32Lib(),
				new DebugLib()));
	}

	private static Globals load(LuaValue... args) {
		Globals g = new Globals();
		for(LuaValue v : args)
			g.load(v);
		LoadState.install(g);
		LuaC.install(g);
		return g;
	}

	private static final String[] UNSAFE = {
			"load",
			"rawget",
			"dofile",
			"loadfile",
			"pcall",
			"rawset",
			"xpcall",
			"setmetatable",
			"collectgarbage",
			"error",
			//"package",
			"getmetatable",
			"_G",
			"bit32"
	};

	public static Globals purify(final Globals g) {
		Stream.of(UNSAFE).forEach(unsafe -> {
			g.set(unsafe, LuaValue.NIL);
		});
		return g;
	}
}
