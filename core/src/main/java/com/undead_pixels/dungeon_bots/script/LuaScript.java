/*
 * Written by UNDEAD PIXELS
 * */


package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import org.luaj.vm2.*;
import java.io.File;
import java.util.Optional;

/**
 * A LuaScript is an asynchronous wrapper around an execution context for a sandbox that is invoked using a
 * LuaSandbox as the Sandbox.
 */
public class LuaScript {

	private final LuaSandbox environment;
	private String script;
	private volatile Varargs varargs;
	private volatile ScriptStatus scriptStatus;
	private volatile LuaError _LuaError;
	private Thread thread;

	/**
	 * Initializes a LuaScript with the provided LuaSandbox and source string
	 * @param env
	 * @param script
	 */
	LuaScript(LuaSandbox env, String script) {
		this.environment = env;
		this.script = script;
		this.scriptStatus = ScriptStatus.READY;
	}

	public LuaScript toFile(File f) {
		throw new RuntimeException("Not Implemented");
	}

	
	/** Starts execution of the sandbox on a separate thread. */
	public synchronized LuaScript start() {
		SecurityContext.set(this.environment);
		// TODO: creating threads is expensive. Make a pool of threads?
		// TODO: sandbox should cache as much of itself as it can. Cache the chunk?
		// TODO: create the chunk and the thread upon setting/reseting the text?

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
			catch (Exception e) {
				scriptStatus = ScriptStatus.ERROR;
			}
		});
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
		} catch (Exception ie) {
		}
		scriptStatus = ScriptStatus.STOPPED;
		return this;
	}

	/** Returns the status of this sandbox. */
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
	 * Forces the thread of this sandbox to rejoin in no more than the given
	 * number of milliseconds.
	 */
	public synchronized LuaScript join(long wait) {
		assert scriptStatus != ScriptStatus.STOPPED;

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

	
	/**Returns the Varargs result, if there is one, or 'False' if execution hasn't 
	 * completed yet or there are no results from the execution.*/
	public synchronized Optional<Varargs> getResults() {
		return Optional.ofNullable(varargs);
	}

	/** Stops all execution and clears any stored values.  The sandbox text
	 * remains unchanged. */
	public void reset(long maxWait) {
		join(maxWait);		
		varargs = null;
		this._LuaError = null;
		this.scriptStatus = ScriptStatus.READY;
	}

	/** Resets this sandbox and updates its text. */
	public void setScript(String newScript) {
		reset(0);
		this.script = newScript;
	}

	
	
	/** Returns the text of this sandbox. */
	public String getScript() {
		return this.script;
	}


	/**
	 *
	 * @return LuaError
	 */
	public LuaError getError() {
		return _LuaError;
	}
}
