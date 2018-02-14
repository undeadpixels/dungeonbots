/*
 * Written by UNDEAD PIXELS
 * */

package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.queueing.Taskable;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import org.luaj.vm2.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Stewart Charles
 * @version 2/1/2018 A LuaScript is an asynchronous wrapper around an execution
 *          context for a sandbox that is invoked using a LuaSandbox.
 */
public class LuaInvocation implements Taskable<LuaSandbox> {

	private final LuaSandbox environment;
	private final String script;
	private final LuaValue chunk;
	private final LuaValue[] args;
	private volatile Varargs varargs;
	private volatile ScriptStatus scriptStatus;
	private volatile LuaError luaError;
	private ArrayList<ScriptEventStatusListener> listeners = new ArrayList<>();

	/**
	 * Initializes a LuaScript with the provided LuaSandbox and source string
	 * 
	 * @param env
	 * @param script
	 */
	LuaInvocation(LuaSandbox env, String script) {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		ScriptEngine se = scriptEngineManager.getEngineByName("lua");

		this.environment = env;
		this.script = script;
		this.args = new LuaValue[] {};

		LuaValue chunk;// TODO: sandbox should cache as much of itself as it can. Cache the chunk?
		try {
			chunk = environment.getGlobals().load(this.script);
			this.scriptStatus = ScriptStatus.READY;
		} catch (LuaError le) {
			// TODO - should we just duck this exception?
			chunk = null;
			scriptStatus = ScriptStatus.LUA_ERROR;
		}
		this.chunk = chunk;
	}

	public LuaInvocation(LuaSandbox env, String functionName, LuaValue[] args) {
		this.environment = env;
		this.script = "";
		this.args = new LuaValue[] {};

		LuaValue chunk;
		try {
			chunk = environment.getGlobals().get(functionName);
			this.scriptStatus = ScriptStatus.READY;
		} catch (LuaError le) {
			// TODO - should we just duck this exception?
			chunk = null;
			scriptStatus = ScriptStatus.LUA_ERROR;
		}
		this.chunk = chunk;
	}

	public LuaInvocation toFile(File f) {
		throw new RuntimeException("Not Implemented");
	}

	
	/**
	 * Executes this lua script in-line
	 */
	public void run() {
		SecurityContext.set(environment);
		try {
			scriptStatus = ScriptStatus.RUNNING;
			varargs = chunk.invoke();
			scriptStatus = ScriptStatus.COMPLETE;
			luaError = null;
		} catch (LuaError le) {
			scriptStatus = ScriptStatus.LUA_ERROR;
			luaError = le;
		} catch (Exception e) {
			scriptStatus = ScriptStatus.ERROR;
		}
	}

	/**
	 * Forces an executing thread to stop. Note that it is impossible to
	 * determine how much of the code assigned to this thread will have
	 * executed, so the sandbox of this thread should be considered to be in a
	 * corrupt state.
	 * 
	 * @return The source LuaScript
	 */
	public synchronized LuaInvocation stop() {
		// TODO - tell the DebugLib to die
		
		/*
		if (thread == null)
			return this;
		thread.interrupt();
		thread.stop();
		try {
			thread.join();
		} catch (Throwable ie) {
		}
		scriptStatus = ScriptStatus.STOPPED;*/
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
	 * Calls join on the contained thread, waiting indefinitely for completion.
	 * 
	 * @return The invoked LuaScript
	 */
	public synchronized LuaInvocation join() {
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
	public synchronized LuaInvocation join(long wait) {
		assert scriptStatus != ScriptStatus.STOPPED;
		
		this.environment.getQueue().update(0.0f);
		
		while(scriptStatus == ScriptStatus.RUNNING || scriptStatus == ScriptStatus.READY); // XXX - busy wait = bad

		return this;
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
	public boolean act(float dt) {
		run();
		
		return true;
	}

	public LuaValue[] getArgs() {
		return args;
	}
	

	@Override
	public boolean preAct() {
		for(ScriptEventStatusListener l: listeners) {
			l.scriptEventStarted(this, ScriptStatus.RUNNING);
		}
		return true;
	}

	
	@Override
	public void postAct() {
		for(ScriptEventStatusListener l: listeners) {
			l.scriptEventFinished(this, getStatus());
		}
	}

	
	public void addListener(ScriptEventStatusListener listener) {
		listeners.add(listener);
	}
}
