package com.undead_pixels.dungeon_bots.scene.level;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.SandboxManager;
import com.undead_pixels.dungeon_bots.script.SandboxedValue;
import org.luaj.vm2.Varargs;

public class Level {
	private final SandboxedValue sandboxedValue;

	public Level(Varargs varargs, LuaSandbox luaSandbox) {
		sandboxedValue = new SandboxedValue(varargs, luaSandbox);
	}

	public Level init() {
		sandboxedValue.invoke(varargs ->
			varargs.checktable(1).get("init").invoke());
		return this;
	}

	public Level update() {
		sandboxedValue.invoke(varargs -> varargs.arg1().get("update").invoke());
		return this;
	}
}
