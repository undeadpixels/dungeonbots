package com.undead_pixels.dungeon_bots.script.environment;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * Example hook function derived from
 * <a>http://www.luaj.org/luaj/3.0/examples/jse/SampleSandboxed.java</a>
 */
public class HookFunction extends ZeroArgFunction {

	private boolean isKilled = false;

	public HookFunction() { }

	public LuaValue call() {
		if(isKilled) { throw new ScriptInterruptException(); }
		synchronized (this) {
			try {
				this.wait();
			}
			catch (InterruptedException ie) {
				throw new ScriptInterruptException();
			}
		}
		return LuaValue.NIL;
	}

	public synchronized void kill() {
		isKilled = true;
	}

	public static class ScriptInterruptException extends RuntimeException { }
}