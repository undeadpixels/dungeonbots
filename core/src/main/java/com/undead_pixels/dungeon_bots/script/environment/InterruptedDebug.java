package com.undead_pixels.dungeon_bots.script.environment;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;

import com.undead_pixels.dungeon_bots.utils.exceptions.ScriptInterruptException;

import java.util.concurrent.Semaphore;
/**
 * Code derived from Stackoverflow response to handling 'terminating' Lua scripts<br>
 * <a>https://stackoverflow.com/questions/17496868/lua-java-luaj-handling-or-interrupting-infinite-loops-and-threads?noredirect=1&lq=1</a>
 */
public class InterruptedDebug extends DebugLib {
	private boolean interrupted = false;
	@Override
	public void onInstruction(int pc, Varargs v, int top) {
		synchronized(this) {
			if (interrupted) {
				throw new ScriptInterruptException();
			}
		}
		super.onInstruction(pc, v, top);
	}

	public synchronized void kill() {
		interrupted = true;
	}
	public synchronized boolean isKilled() {
		return interrupted;
	}
}