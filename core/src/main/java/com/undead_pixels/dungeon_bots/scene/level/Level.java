package com.undead_pixels.dungeon_bots.scene.level;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.SandboxedValue;
import java.io.File;

public class Level {
	private static final long MAX_WAIT = 10000;

	/**
	 * A Varargs value that is associated with a LuaSandbox environment.
	 * The Varargs contained in the SandboxedValue is assumed to be a LuaTable;
	 */
	private final SandboxedValue map;

	public Level(LuaSandbox luaSandbox, File f) {
		// TODO : Make this initialization non-blocking!
		map = luaSandbox.script(f).start().join(MAX_WAIT).getSandboxedValue().get();
		assert map.getResult().arg1().istable();
	}

	public Level init() {
		map.getLuaSandbox().invoke(() -> map.getResult().arg1().checktable().get("init").invoke());
		return this;
	}

	public Level update() {
		map.getLuaSandbox().invoke(() -> map.getResult().arg1().checktable().get("update").invoke());
		return this;
	}
}
