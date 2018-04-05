/*
 * Written by UNDEAD PIXELS
 * */

package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.queueing.Taskable;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.HasImage;
import com.undead_pixels.dungeon_bots.script.environment.InterruptedDebug;
import com.undead_pixels.dungeon_bots.utils.exceptions.ScriptInterruptException;

import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author Stewart Charles
 * @version 2/1/2018 A LuaScript is an asynchronous wrapper around an execution
 *          context for a sandbox that is invoked using a LuaSandbox.
 */
public class LuaInvocation implements Taskable<LuaSandbox> {

	/**
	 * The environment this lives in
	 */
	private final LuaSandbox environment;
	
	/**
	 * The function calls this invocation makes 
	 */
	private final LuaValue[] functions;
	
	/**
	 * The arguments passed to those functions
	 */
	private final LuaValue[] args;
	
	/**
	 * The result of this invocation
	 */
	private volatile Varargs result;
	
	/**
	 * The status of this invocation
	 */
	private volatile ScriptStatus scriptStatus;
	
	/**
	 * The error thrown by this invocation (if any)
	 */
	private volatile LuaError luaError;
	
	/**
	 * Things listening for the status of this invocation
	 */
	private ArrayList<ScriptEventStatusListener> listeners = new ArrayList<>();
	
	/**
	 * Max number of instructions
	 */
	private final static int MAX_INSTRUCTIONS = 1000;
	
	/**
	 * A handle to interrupt the execution of this invocation
	 */
	private InterruptedDebug scriptInterrupt = new InterruptedDebug();

	/**
	 * Initializes a LuaScript with the provided LuaSandbox and source string
	 * 
	 * @param env
	 * @param script
	 */
	LuaInvocation(LuaSandbox env, String script) {
		this.environment = env;
		this.args = new LuaValue[] {};
		this.scriptStatus = ScriptStatus.READY;
		LuaValue[] functions;
		try {
			functions = new LuaValue[] {environment.invokerGlobals.load(script, "main", environment.getGlobals())};
		} catch(LuaError e) {
			luaError = e;
			scriptStatus = ScriptStatus.LUA_ERROR;
			functions = new LuaValue[] {};
			// TODO - why not just duck this error?
		}
		this.functions = functions;
	}

	public LuaInvocation(LuaSandbox env, LuaValue[] functions, LuaValue[] args) {
		this.environment = env;
		this.functions = functions;
		this.args = args;
		this.scriptStatus = ScriptStatus.READY;
	}

	public LuaInvocation(LuaSandbox env, Collection<LuaValue> functions, LuaValue[] args) {
		this(env, functions.toArray(new LuaValue[0]), args);
	}

	public LuaInvocation(LuaSandbox env, UserScript script) {
		this(env, script == null ? "" : script.code);
	}

	/**
	 * Executes this lua script in-line
	 */
	public void run() {
		if(scriptStatus == ScriptStatus.LUA_ERROR) {
			return;
		}
		
		// TODO - maybe add the current thread to the sandbox map?
		SandboxManager.register(Thread.currentThread(), this.environment); // TODO - or should we delete this?
		try {
			setStatus(ScriptStatus.RUNNING);

			/* Initialize new HookFunction and InterruptedDebug every time run() is called */
			ZeroArgFunction zeroArg = new ZeroArgFunction() {
				@Override
				public LuaValue call () {
					return LuaValue.NIL;
				}
			};

			/* Setup default globals */
			environment.getGlobals().load(scriptInterrupt);
			LuaValue setHook = environment.getGlobals().get("debug").get("sethook");
			environment.getGlobals().set("debug", LuaValue.NIL);
			for(LuaValue chunk : functions) {
				LuaThread thread = new LuaThread(environment.getGlobals(), chunk);
				setHook.invoke(LuaValue.varargsOf(new LuaValue[]{
						thread, zeroArg, LuaValue.EMPTYSTRING, LuaValue.valueOf(MAX_INSTRUCTIONS)} ));

				// When errors occur in LuaThread, they don't cause this thread to throw a LuaError exception.
				// Instead the varargs returns with a false boolean as the first result.
				Varargs ans = thread.resume(LuaValue.NIL);
				result = ans.subargs(2);
				if(ans.arg1().checkboolean()) {
					//setStatus(ScriptStatus.COMPLETE);
					luaError = null;
				}
				else {
					final String errString = ans.arg(2).checkjstring();
					final ScriptStatus scriptStatus = errString.contains("ScriptInterruptException")
							? ScriptStatus.STOPPED :
							ScriptStatus.LUA_ERROR;
					setStatus(scriptStatus);
					luaError = new LuaError(errString);
					environment.getSecurityContext().getWorld()
							.ifPresent(w -> w.message(
									environment.getSecurityContext().getEntity(),
									luaError.getMessage(),
									LoggingLevel.ERROR));
					break;
				}
			}
		}
		catch (ScriptInterruptException si) {
			if(getStatus() != ScriptStatus.TIMEOUT) {
				setStatus(ScriptStatus.STOPPED);
				environment.getSecurityContext()
						.getWorld()
						.ifPresent(w ->
								w.message(
										environment.getSecurityContext().getEntity(),
										"Script Stopped",
										LoggingLevel.GENERAL));
			}
		}
		catch(LuaError le ) {
			setStatus(ScriptStatus.LUA_ERROR);
			environment.getSecurityContext()
					.getWorld()
					.ifPresent(w ->
							w.message(
									environment.getSecurityContext().getEntity(),
									le.getMessage(),
									LoggingLevel.ERROR));
			luaError = le;
		}
		catch (Throwable t) {
			setStatus(ScriptStatus.ERROR);
			environment.getSecurityContext()
					.getWorld()
					.ifPresent(w ->
							w.message(
									environment.getSecurityContext().getEntity(),
									t.getMessage(),
									LoggingLevel.ERROR));
		}
		
		if(getStatus() == ScriptStatus.RUNNING) {
			setStatus(ScriptStatus.COMPLETE);
		}

		synchronized (this) { this.notifyAll(); }
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
		this.notifyAll();
		
		this.join();
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
	 * @param newStatus	The new status of this script
	 */
	private synchronized void setStatus(ScriptStatus newStatus) {
		scriptStatus = newStatus;
	}

	/**
	 * Calls join on the contained thread, waiting indefinitely for completion.
	 * @return The invoked LuaScript
	 */
	public LuaInvocation join() {
		return join(-1);
	}

	/**
	 * Calls join on the contained thread. Call with '0' to wait indefinitely.
	 * @param timeout The amount of time to wait for the join
	 * @return The invoked LuaScript
	 */
	public synchronized LuaInvocation join(long timeout) {
		if(scriptStatus != ScriptStatus.READY && scriptStatus != ScriptStatus.RUNNING) {
			return this;
		}
		
		if(timeout <= 0) {
			timeout = 60000; // this is a long time...
		}
		
		try {
			if(scriptStatus == ScriptStatus.READY ||
					scriptStatus == ScriptStatus.RUNNING) {
				this.environment.update(0.0f);
				
				long startTime = System.currentTimeMillis();
				while(System.currentTimeMillis()-startTime < timeout) {
					long waitTime = (System.currentTimeMillis()-startTime);
					
					if(waitTime < 1) {
						waitTime = 1;
					}
					
					this.wait(timeout - waitTime);
					
					if(scriptStatus != ScriptStatus.READY &&
							scriptStatus !=  ScriptStatus.RUNNING) {
						
						break; // script finished
					}
				}
				
				if(scriptStatus == ScriptStatus.READY ||
						scriptStatus ==  ScriptStatus.RUNNING) {
					scriptInterrupt.kill();
					this.notifyAll();
					scriptStatus = ScriptStatus.TIMEOUT; // ok to set directly as we already have it locked
				}
				
			} else {
				// already finished
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

		synchronized(this) {
			this.notifyAll();
		}
	}

	
	/**
	 * @param listener	Something that will listen for when this invocation has finished
	 */
	public void addListener(ScriptEventStatusListener listener) {
		listeners.add(listener);
	}

	/**
	 * @param time
	 */
	public synchronized void safeSleep (long time) {
		if(scriptInterrupt.isKilled()) {
			throw new ScriptInterruptException();
		} else if(time <= 0) {
			return;
		} else {
			try {
				this.wait(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(scriptInterrupt.isKilled()) {
			throw new ScriptInterruptException();
		} 
	}
}
