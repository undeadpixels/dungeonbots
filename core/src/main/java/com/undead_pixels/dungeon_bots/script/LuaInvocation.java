/*
 * Written by UNDEAD PIXELS
 * */

package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.queueing.Taskable;
import com.undead_pixels.dungeon_bots.script.environment.HookFunction;
import com.undead_pixels.dungeon_bots.script.environment.InterruptedDebug;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import org.luaj.vm2.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Stewart Charles
 * @version 2/1/2018 A LuaScript is an asynchronous wrapper around an execution
 *          context for a sandbox that is invoked using a LuaSandbox.
 */
public class LuaInvocation implements Taskable<LuaSandbox> {

	private final LuaSandbox environment;
	private final String script;
	private final LuaValue[] args;
	private volatile Varargs varargs;
	private volatile ScriptStatus scriptStatus;
	private volatile LuaError luaError;
	private ArrayList<ScriptEventStatusListener> listeners = new ArrayList<>();
	private final static int MAX_INSTRUCTIONS = 1000;
	private HookFunction hookFunction;
	private InterruptedDebug scriptInterrupt;
	private Lock statusLock = new ReentrantLock();

	/**
	 * Initializes a LuaScript with the provided LuaSandbox and source string
	 * 
	 * @param env
	 * @param script
	 */
	LuaInvocation(LuaSandbox env, String script) {
		this.environment = env;
		this.script = script;
		this.args = new LuaValue[] {};
	}

	public LuaInvocation(LuaSandbox env, String functionName, LuaValue[] args) {
		this.environment = env;
		this.script = "";
		this.args = new LuaValue[] {};
	}

	public LuaInvocation toFile(File f) {
		throw new RuntimeException("Not Implemented");
	}

	
	/**
	 * Executes this lua script in-line
	 */
	public void run() {
		SecurityContext.set(environment);
		statusLock.lock();
		try {
			if (scriptStatus == ScriptStatus.LUA_ERROR) {
				synchronized (this) {
					this.notifyAll();
				}
				return;
			}
		}
		finally {
			statusLock.unlock();
		}
		try {
			statusLock.lock();
			scriptStatus = ScriptStatus.RUNNING;
			statusLock.unlock();

			/* Initialize new HookFunction and InterruptedDebug every time run() is called */
			hookFunction = new HookFunction();
			scriptInterrupt = new InterruptedDebug();

			environment.getGlobals().load(scriptInterrupt);
			LuaValue setHook = environment.getGlobals().get("debug").get("sethook");
			environment.getGlobals().set("debug", LuaValue.NIL);

			// Use our own print and printf functions
			environment.getGlobals().set("print", environment.getPrintFunction());
			environment.getGlobals().set("printf", environment.getPrintfFunction());

			LuaValue chunk = environment.invokerGlobals.load(this.script, "main", environment.getGlobals());
			LuaThread thread = new LuaThread(environment.getGlobals(), chunk);
			setHook.invoke(LuaValue.varargsOf(new LuaValue[]{
					thread, hookFunction, LuaValue.EMPTYSTRING, LuaValue.valueOf(MAX_INSTRUCTIONS)} ));

			// When errors occur in LuaThread, they don't cause this thread to throw a LuaError exception.
			// Instead the varargs returns with a false boolean as the first result.
			Varargs ans = thread.resume(LuaValue.NIL);
			varargs = ans.subargs(2);
			statusLock.lock();
			try {
				if (ans.arg1().checkboolean()) {
					scriptStatus = ScriptStatus.COMPLETE;
					luaError = null;
				} else {
					scriptStatus = ans.arg(2).checkjstring().contains("ScriptInterruptException") ?
							ScriptStatus.STOPPED :
							ScriptStatus.LUA_ERROR;
					luaError = new LuaError(ans.arg(2).checkjstring());
				}
			}
			finally {
				statusLock.unlock();
			}
		}
		catch(LuaError le ) {
			statusLock.lock();
			scriptStatus = ScriptStatus.LUA_ERROR;
			luaError = le;
			statusLock.unlock();
		}
		catch (InstructionHook.ScriptInterruptException si) {
			statusLock.lock();
			scriptStatus = ScriptStatus.STOPPED;
			statusLock.unlock();
		}
		catch (Exception e) {
			statusLock.lock();
			scriptStatus = ScriptStatus.ERROR;
			statusLock.unlock();
		}
		finally {
			synchronized (this) {
				this.notifyAll();
			}
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
		scriptInterrupt.kill();
		//hookFunction.kill();
		try { this.wait(); }
		catch (InterruptedException ie) { }
		return this;
	}

	/**
	 * Returns an enum of the Scripts status<br>
	 * Values: READY, RUNNING, STOPPED, LUA_ERROR,ERROR,TIMEOUT,PAUSED,COMPLETE
	 * 
	 * @return Returns the status of this sandbox.
	 */
	public synchronized ScriptStatus getStatus() {
		statusLock.lock();
		try { return scriptStatus; }
		finally { statusLock.unlock(); }
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
	public LuaInvocation join(long wait) {
		synchronized (this) {
			statusLock.lock();
			try {
				if (scriptStatus != ScriptStatus.RUNNING)
					return this;
			}
			finally {
				statusLock.unlock();
			}
			this.environment.getQueue().update(0.0f);
			try {
				this.wait(wait);
			} catch (InterruptedException ie) {
			}
			statusLock.lock();
			if (this.scriptStatus == ScriptStatus.RUNNING)
				this.scriptStatus = ScriptStatus.TIMEOUT;
			statusLock.unlock();
		}
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
		synchronized (this) {
			this.notifyAll();
		}
	}

	
	public void addListener(ScriptEventStatusListener listener) {
		listeners.add(listener);
	}
}
