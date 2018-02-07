/*
 * Written by UNDEAD PIXELS
 * */

package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import org.luaj.vm2.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.Optional;

/**
 * @author Stewart Charles
 * @version 2/1/2018 A LuaScript is an asynchronous wrapper around an execution
 *          context for a sandbox that is invoked using a LuaSandbox.
 */
public class LuaScript extends ScriptEvent {

	private final LuaSandbox environment;
	private final String script;
	private volatile Varargs varargs;
	private volatile ScriptStatus scriptStatus;
	private volatile LuaError luaError;
	private Thread thread;

	/**
	 * Initializes a LuaScript with the provided LuaSandbox and source string
	 * 
	 * @param env
	 * @param script
	 */
	LuaScript(LuaSandbox env, String script) {
		super(env);
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine se = scriptEngineManager.getEngineByName("lua");

		this.environment = env;
		this.script = script;
		this.scriptStatus = ScriptStatus.READY;
	}

	public LuaScript toFile(File f) {
		throw new RuntimeException("Not Implemented");
	}

	/** Starts execution of the sandbox on a separate thread. */
	public synchronized LuaScript start() {
		// TODO: creating threads is expensive. Make a pool of threads?
		// TODO: sandbox should cache as much of itself as it can. Cache the chunk?
		// TODO: create the chunk and the thread upon setting/reseting the text?

		thread = ThreadWrapper.create(() -> {
			SecurityContext.set(environment);
			try {
				scriptStatus = ScriptStatus.RUNNING;
				LuaValue chunk = environment.getGlobals().load(this.script);
				varargs = chunk.invoke();
				scriptStatus = ScriptStatus.COMPLETE;
				luaError = null;
			} catch (LuaError le) {
				scriptStatus = ScriptStatus.LUA_ERROR;
				luaError = le;
			} catch (Exception e) {
				scriptStatus = ScriptStatus.ERROR;
			}
		});
		if (scriptStatus == ScriptStatus.READY || scriptStatus == ScriptStatus.COMPLETE)
			thread.start();
		return this;
	}

	/**
	 * Forces an executing thread to stop. Note that it is impossible to
	 * determine how much of the code assigned to this thread will have
	 * executed, so the sandbox of this thread should be considered to be in a
	 * corrupt state.
	 * 
	 * @return The source LuaScript
	 */
	public synchronized LuaScript stop() {
		if (thread == null)
			return this;
		thread.interrupt();
		thread.stop();
		try {
			thread.join();
		} catch (Throwable ie) {
		}
		scriptStatus = ScriptStatus.STOPPED;
		return this;
	}

	/**
	 * Returns an enum of the Scripts status<br>
	 * Values: READY, RUNNING, STOPPED, LUA_ERROR,ERROR,TIMEOUT,PAUSED,COMPLETE
	 * 
	 * @return Returns the status of this sandbox.
	 */
	public synchronized ScriptStatus getStatus() {
		return scriptStatus;
	}

	/**
	 *
	 * @return
	 */
	@Deprecated
	public synchronized LuaScript resume() {
		throw new RuntimeException("Not Implemented");
	}

	/**
	 * @return Returns the invoked Script
	 */
	@Deprecated
	public synchronized LuaScript pause() {
		throw new RuntimeException("Not Implemented");
	}

	/**
	 * Calls join on the contained thread, waiting indefinitely for completion.
	 * 
	 * @return The invoked LuaScript
	 */
	public synchronized LuaScript join() {
		// TODO: the Script should manage its thread internally, but expose a
		// reset()
		return join(0);
	}

	/**
	 * Calls join on the contained thread. Call with '0' to wait indefinitely.
	 * 
	 * @param wait
	 *            The amount of time to wait for the join
	 * @return The invoked LuaScript
	 */
	public synchronized LuaScript join(long wait) {
		assert scriptStatus != ScriptStatus.STOPPED;

		// TODO: the Script should manage its thread internally, but expose a
		// reset()
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

	/**
	 * Returns an Optional that may contain the invoked results of the LuaScript
	 * 
	 * @return An Optional containing results if they are present.
	 */
	public Optional<Varargs> getResults() {
		return Optional.ofNullable(varargs);
	}

	/**
	 * Stops all execution and clears any stored values. The sandbox text
	 * remains unchanged.
	 */
	public void reset(long maxWait) {
		join(maxWait);
		varargs = null;
		this.luaError = null;
		this.scriptStatus = ScriptStatus.READY;
	}

	/**
	 * Returns the current LuaError
	 * 
	 * @return LuaError
	 */
	public LuaError getError() {
		return luaError;
	}

	public Optional<SandboxedValue> getSandboxedValue() {
		return getResults().map(val -> new SandboxedValue(val, environment));
	}

	@Override
	public boolean startScript() {
		return false;
	}

	@Override
	public boolean preAct() {
		return true;
	}

	@Override
	public boolean act(float t) {
		return true;
	}

	@Override
	public void postAct() {

	}
}
