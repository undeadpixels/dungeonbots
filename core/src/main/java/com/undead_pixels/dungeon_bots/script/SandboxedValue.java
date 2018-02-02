package com.undead_pixels.dungeon_bots.script;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class SandboxedValue {
	private final LuaValue luaValue;
	private final LuaSandbox luaSandbox;

	public SandboxedValue(LuaValue luaValue, LuaSandbox luaSandbox) {
		this.luaValue = luaValue;
		this.luaSandbox = luaSandbox;
	}

	public LuaValue getLuaValue() {
		return luaValue;
	}

	public LuaSandbox getLuaSandbox() {
		return luaSandbox;
	}

	public Varargs invoke() {
		return luaValue.invoke();
	}

	public Varargs invoke(LuaValue... vals) {
		return luaValue.invoke(vals);
	}

	public Varargs invoke(Varargs varargs) {
		return this.luaValue.invoke(varargs);
	}
}
