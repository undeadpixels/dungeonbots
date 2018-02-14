package com.undead_pixels.dungeon_bots.script.environment;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.concurrent.*;

public class HookFunction extends ZeroArgFunction {

	private boolean isKilled = false;
	private final Semaphore semaphore;

	public HookFunction(final Semaphore semaphore) {
		this.semaphore = semaphore;
	}

	public LuaValue call() {
		if(isKilled) { throw new ScriptInterruptException(); }
		return LuaValue.NIL;
	}

	public synchronized void kill() {
		isKilled = true;
	}

	public static class ScriptInterruptException extends RuntimeException { }
}