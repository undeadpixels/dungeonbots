/*
 * Written by UNDEAD PIXELS
 * */

package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.queueing.Taskable;
import com.undead_pixels.dungeon_bots.script.environment.HookFunction;
import com.undead_pixels.dungeon_bots.script.environment.InterruptedDebug;
import org.luaj.vm2.*;
import java.util.*;

/**
 * @author Stewart Charles
 * @version 2/1/2018 A LuaScript is an asynchronous wrapper around an execution
 *          context for a sandbox that is invoked using a LuaSandbox.
 */
public class LuaInvocation implements Taskable<LuaSandbox> {

	private final LuaSandbox environment;
	private final LuaValue[] functions;
	private final LuaValue[] args;
	private volatile Varargs result;
	private volatile ScriptStatus scriptStatus;
	private volatile LuaError luaError;
	private ArrayList<ScriptEventStatusListener> listeners = new ArrayList<>();
	private final static int MAX_INSTRUCTIONS = 1000;
	private HookFunction hookFunction;
	private InterruptedDebug scriptInterrupt = new InterruptedDebug();
	private final String name;
	
	private final Object joinNotificationObj = new Object();

	/**
	 * Initializes a LuaScript with the provided LuaSandbox and source string
	 * 
	 * @param env
	 * @param script
	 */
	LuaInvocation(LuaSandbox env, String script) {
		this.environment = env;
		this.functions = new LuaValue[] {environment.invokerGlobals.load(script, "main", environment.getGlobals())};
		this.args = new LuaValue[] {};
		this.scriptStatus = ScriptStatus.READY;
		this.name = "string invocation";
	}

	public LuaInvocation(LuaSandbox env, LuaValue[] functions, LuaValue[] args) {
		this.environment = env;
		this.functions = functions;
		this.args = args;
		this.scriptStatus = ScriptStatus.READY;
		this.name = "functionarr invocation";
	}

	public LuaInvocation(LuaSandbox env, Collection<LuaValue> functions, LuaValue[] args) {
		this(env, functions.toArray(new LuaValue[0]), args);
	}

	public LuaInvocation(LuaSandbox env, UserScript script) {
		this(env, script.code);
	}

	/**
	 * Executes this lua script in-line
	 */
	public void run() {
		if(scriptStatus == ScriptStatus.LUA_ERROR) {
			return;
		}
		System.out.println("Invoking "+name+" ("+functions.length+" count)");
		
		// TODO - maybe add the current thread to the sandbox map?
		SandboxManager.register(Thread.currentThread(), this.environment); // TODO - or should we delete this?
		try {
			setStatus(ScriptStatus.RUNNING);

			/* Initialize new HookFunction and InterruptedDebug every time run() is called */
			hookFunction = new HookFunction();

			/* Setup default globals */
			environment.getGlobals().load(scriptInterrupt);
			LuaValue setHook = environment.getGlobals().get("debug").get("sethook");
			environment.getGlobals().set("debug", LuaValue.NIL);
			
			for(LuaValue chunk : functions) {
				LuaThread thread = new LuaThread(environment.getGlobals(), chunk);
				setHook.invoke(LuaValue.varargsOf(new LuaValue[]{
						thread, hookFunction, LuaValue.EMPTYSTRING, LuaValue.valueOf(MAX_INSTRUCTIONS)} ));

				// When errors occur in LuaThread, they don't cause this thread to throw a LuaError exception.
				// Instead the varargs returns with a false boolean as the first result.
				Varargs ans = thread.resume(LuaValue.NIL);
				result = ans.subargs(2);
				if(ans.arg1().checkboolean()) {
					//setStatus(ScriptStatus.COMPLETE);
					luaError = null;
				} else {
					setStatus(ans.arg(2).checkjstring().contains("ScriptInterruptException") ?
							ScriptStatus.STOPPED :
							ScriptStatus.LUA_ERROR);
					luaError = new LuaError(ans.arg(2).checkjstring());
					synchronized (this) { this.notifyAll(); }
					break;
				}
			}
		}
		catch(LuaError le ) {
			setStatus(ScriptStatus.LUA_ERROR);
			luaError = le;
		}
		catch (InstructionHook.ScriptInterruptException si) {
			if(getStatus() != ScriptStatus.TIMEOUT)
				setStatus(ScriptStatus.STOPPED);
			synchronized (this) { this.notifyAll(); }
		}
		catch (Exception e) {
			setStatus(ScriptStatus.ERROR);
		}
		
		if(getStatus() == ScriptStatus.RUNNING) {
			setStatus(ScriptStatus.COMPLETE);
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
		scriptInterrupt.kill();
		
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
		synchronized(joinNotificationObj) {
			return scriptStatus;
		}
	}

	private synchronized void setStatus(ScriptStatus newStatus) {
		synchronized(joinNotificationObj) {
			scriptStatus = newStatus;
		}
	}

	/**
	 * Calls join on the contained thread, waiting indefinitely for completion.
	 * @return The invoked LuaScript
	 */
	public LuaInvocation join() {
		// TODO: the Script should manage its thread internally, but expose a
		// reset()
		return join(-1);
	}

	/**
	 * Calls join on the contained thread. Call with '0' to wait indefinitely.
	 * @param timeout The amount of time to wait for the join
	 * @return The invoked LuaScript
	 */
	public LuaInvocation join(long timeout) {
		assert scriptStatus != ScriptStatus.STOPPED;

		try {
			synchronized(joinNotificationObj) {
				if(scriptStatus == ScriptStatus.READY ||
						scriptStatus == ScriptStatus.RUNNING) {
					this.environment.update(0.0f);
					if(timeout > 0) {
						joinNotificationObj.wait(timeout);

						if(scriptStatus == ScriptStatus.READY ||
								scriptStatus == ScriptStatus.RUNNING) {
							scriptInterrupt.kill();
							scriptStatus = ScriptStatus.TIMEOUT; // ok to set directly as we already have it locked
						}
					} else {
						joinNotificationObj.wait();
					}

				} else {
					// already finished
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return this;
	}

	/**
	 * Returns an Optional that may contain the invoked results of the LuaScript
	 * 
	 * @return An Optional containing results if they are present.
	 */
	public Optional<Varargs> getResults() {
		return Optional.ofNullable(result);
	}

	/**
	 * Stops all execution and clears any stored values. The sandbox text
	 * remains unchanged.
	 */
	public void reset(long maxWait) {
		join(maxWait);
		result = null;
		this.luaError = null;
		setStatus(ScriptStatus.READY);
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

		synchronized(joinNotificationObj) {
			joinNotificationObj.notifyAll();
		}
	}

	
	public void addListener(ScriptEventStatusListener listener) {
		listeners.add(listener);
	}
}
