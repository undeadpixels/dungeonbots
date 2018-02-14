package com.undead_pixels.dungeon_bots.script.environment;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;

import java.util.concurrent.Semaphore;
/**
 * Code derived from Stackoverflow response to handling 'terminating' Lua scripts<br>
 * <a>https://stackoverflow.com/questions/17496868/lua-java-luaj-handling-or-interrupting-infinite-loops-and-threads?noredirect=1&lq=1</a>
 */
public class InterruptedDebug extends DebugLib {
	public boolean interrupted = false;
	public final Semaphore yeildLock = new Semaphore(1, true);
	@Override
	public void onInstruction(int pc, Varargs v, int top) {
		try {
			yeildLock.acquire();
		}
		catch (InterruptedException ie) {
			throw new ScriptInterruptException();
		}
		if (interrupted) {
			throw new ScriptInterruptException();
		}
		super.onInstruction(pc, v, top);
	}

	public static class ScriptInterruptException extends RuntimeException {}
}