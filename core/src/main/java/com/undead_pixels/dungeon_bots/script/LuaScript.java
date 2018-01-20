/*
 * Written by UNDEAD PIXELS
 * */


package com.undead_pixels.dungeon_bots.script;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.File;
import java.util.Optional;

public class LuaScript {

	private final LuaScriptEnvironment environment;
	private String script;
	private volatile Varargs varargs;
	private volatile ScriptStatus scriptStatus;
	private Thread thread;
	private LuaError _LuaError;

	public LuaScript(LuaScriptEnvironment env, String script) {
		this.environment = env;
		this.script = script;
		this.scriptStatus = ScriptStatus.READY;
	}

	public LuaScript toFile(File f) {
		throw new RuntimeException("Not Implemented");
	}

	
	/** Starts execution of the script on another thread. */
	public synchronized LuaScript start() {

		// TODO: creating threads is expensive. Make a pool of threads?
		// TODO: script should cache as much of itself as it can. Cache the chunk?
		// TODO: create the chunk and the thread upon setting/reseting the text?

		if (thread == null) {
			thread = ThreadWrapper.create(() -> {
				try {
					scriptStatus = ScriptStatus.RUNNING;
					LuaValue chunk = environment.getGlobals().load(this.script);
					varargs = chunk.invoke();
					scriptStatus = ScriptStatus.COMPLETE;
					_LuaError = null;
				} catch (LuaError le) {
					scriptStatus = ScriptStatus.LUA_ERROR;
					_LuaError = le;
				}
			});
		}
		if (scriptStatus == ScriptStatus.READY || scriptStatus == ScriptStatus.COMPLETE)
			thread.start();
		return this;
	}

	/** Forces an executing thread to stop. */
	public synchronized LuaScript stop() {
		if (thread == null)
			return this;
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException ie) {
		}
		scriptStatus = ScriptStatus.STOPPED;
		return this;
	}

	/** Returns the status of this script. */
	public synchronized ScriptStatus getStatus() {
		return scriptStatus;
	}

	/** Resumes execution of a thread paused. */
	public synchronized LuaScript resume() {
		throw new RuntimeException("Not Implemented");
	}

	/** Pauses execution of a thread. */
	public synchronized LuaScript pause() {
		throw new RuntimeException("Not Implemented");
	}

	public synchronized LuaScript join() {
		//TODO:  the Script should manage its thread internally, but expose a reset()
		return join(0);
	}

	/**
	 * Forces the thread of this script to rejoin in no more than the given
	 * number of milliseconds.
	 */
	public synchronized LuaScript join(long wait) {
		//TODO:  the Script should manage its thread internally, but expose a reset()
		try {
			thread.join(wait);
			if (thread.isAlive())
				scriptStatus = ScriptStatus.TIMEOUT;
			return this;
		} catch (Exception e) {
			scriptStatus = ScriptStatus.ERROR;
			return this;
		}
	}

	
	/**Returns the Varargs result, if there is one, or null if execution hasn't 
	 * completed yet.*/
	public synchronized Optional<Varargs> getResults() {
		return Optional.ofNullable(varargs);
	}

	/** Stops all execution and clears any stored values.  The script text 
	 * remains unchanged. */
	public void reset(long maxWait) {
		join(maxWait);
		this.scriptStatus = ScriptStatus.READY;
		varargs = null;
		this._LuaError = null;
	}

	/** Resets this script and updates its text. */
	public void setScript(String newScript) {
		reset(0);
		this.script = newScript;
	}

	
	
	/** Returns the text of this script. */
	public String getScript() {
		return this.script;
	}

	

	/**Returns the error generated within the Lua script on execution.  If no 
	 * error is generated, this value will be null.*/
	public LuaError getError() {
		return _LuaError;
	}
}
