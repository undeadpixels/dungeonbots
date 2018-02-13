package com.undead_pixels.dungeon_bots.script.environment;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class HookFunction extends ZeroArgFunction {

	private boolean isKilled = false;

	public LuaValue call() {
		if(isKilled) { throw new ScriptInterruptException(); }
		return LuaValue.NIL;
	}

	public static class ScriptInterruptException extends RuntimeException { }

	public synchronized void kill() {
		isKilled = true;
	}
}