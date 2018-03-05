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
		synchronized (this) {
			if(isKilled) {
				System.out.println("Lua Script STOPPED!");
				throw new ScriptInterruptException();
			}
			/*
			try {
				this.wait();
			}
			catch (InterruptedException ie) {
				throw new ScriptInterruptException();
			}*/
		}
		return this;
	}

	public synchronized void kill() {
		isKilled = true;
	}

	public static class ScriptInterruptException extends RuntimeException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}
}