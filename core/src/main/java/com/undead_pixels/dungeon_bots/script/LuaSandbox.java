package com.undead_pixels.dungeon_bots.script;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.queueing.CoalescingGroup;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.events.ScriptEventQueue;
import com.undead_pixels.dungeon_bots.script.proxy.LuaBinding;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.*;
/**
 * @author Stewart Charles
 * @version 2/1/2018
 * A LuaSandbox is a factory for creating a Sandbox of globals and methods that can be used to invoke
 * LuaScripts. A LuaSandbox is essentially a collection of allowed Lua functions.<br>
 * LuaSandbox's manage setting up the SecurityContext for the invoked LuaScripts when they are called.
 * TODO: May need to generate a return type from LuaScripts that references the source LuaSandbox
 */
public class LuaSandbox {

	private final Globals globals;
	final Globals invokerGlobals = JsePlatform.debugGlobals();
	private final SecurityContext securityContext; // TODO - init this
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
	private final ScriptEventQueue scriptQueue = new ScriptEventQueue(this);
	private final List<Consumer<String>> outputEventListeners = new ArrayList<>();

	/**
	 * Initializes a LuaSandbox using JsePlatform.standardGloabls() as the Globals
	 */
	public LuaSandbox() {
		this(SecurityLevel.NONE);
	}

	/**
	 * Creates a new LuaSandbox using different enumerated default Global environments specified by the Sandbox parameter
	 * @param securityLevel An enumeration of different default Global environment types to use for the Script environment
	 */
	public LuaSandbox(SecurityLevel securityLevel) {
		this(securityLevel, securityLevel.globals);
	}

	/**
	 * Inits a LuaSandbox using the argument Globals parameter
	 * @param globals
	 */
	public LuaSandbox(Globals globals) {
		this(SecurityLevel.NONE, globals);
	}

	public LuaSandbox(SecurityLevel securityLevel, Globals globals) {
		this.securityLevel = securityLevel;
		this.globals = globals;
	}


	/**
	 * Adds a Collection of LuaBindings to the Global Environment for the LuaSandbox.
	 * @param bindings A collection of LuaBindings to append to the Global Environment
	 * @return The modified LuaSandbox
	 */
	private LuaSandbox add(Stream<LuaBinding> bindings) {
        bindings.forEach(binding ->
				globals.set(binding.bindTo, binding.luaValue));
        return this;
    }


	public LuaSandbox setSecurityLevel(SecurityLevel securityLevel) {
		this.securityLevel = securityLevel;
		return this;
	}

	/**
	 * Adds the bindings of the argument collection of Bindable objects to the source LuaSandbox
	 * @param bindable A Collection of Objects that implement the GetLuaFacade interface
	 * @param <T> A Type that implements the GetLuaFacade interface
	 * @return The source LuaSandbox
	 */
    @SafeVarargs
    public final <T extends GetLuaFacade> LuaSandbox  addBindable(T... bindables) {
		whitelist.addAutoLevelsForBindables(bindables);
		add(Stream.of(bindables)
				.map(GetLuaFacade::getLuaBinding));
		return this;
	}

	/**
	 * Adds the static bindings of the argument Class
	 * @param clz The Class to add the static Bindings of
	 * @return The source LuaSandbox
	 */
	@SafeVarargs
	public final LuaSandbox addBindableClass(final Class<? extends GetLuaFacade>... clz) {
		for(Class<? extends GetLuaFacade> c : clz) {
			whitelist.addAutoLevelsForBindables(c);
			add(LuaProxyFactory.getBindings(c));
		}
		return this;
	}

	/**
	 * Variadic method for adding the argument LuaBindings to the LuaSandbox. Essentially an overload of
	 * the add(Collection:LuaBinding) method
	 * @param bindings A variable number of LuaBinding parameters
	 * @return The modified LuaSandbox
	 */
	public LuaSandbox add(LuaBinding... bindings) {
        return add(Stream.of(bindings));
    }

	/**
	 * @param script
	 * @return
	 */
	public LuaInvocation init(String script, ScriptEventStatusListener... listeners) {
		LuaInvocation ret = this.enqueueCodeBlock(script, listeners);
		scriptQueue.update(0.f);
		
		return ret;
	}

	/**
	 * @param scriptFile
	 * @return
	 */
	public LuaInvocation init(File scriptFile, ScriptEventStatusListener... listeners) {
		LuaInvocation ret = this.enqueueCodeBlock(scriptFile, listeners);
		scriptQueue.update(0.f);
		
		return ret;
	}
	
    /**
     * Accessor for the Globals for the LuaSandbox
     * @return The Globals for the LuaSandbox
     */
    public Globals getGlobals() {
        return globals;
    }

	/**
	 * Get the Whitelist of the LuaSandbox
	 * @return
	 */
	public Whitelist getWhitelist() {
		return whitelist;
	}

	/**
	 * Get the SecurityLevel of the LuaSandbox
	 * @return
	 */
	public SecurityLevel getSecurityLevel() {
		return securityLevel;
	}
	
	public ScriptEventQueue getQueue() {
		return scriptQueue;
	}

	
	/**
	 * Enqueues a lua function call
	 * 
	 * @param functionName	Name of the function to call
	 * @param args			Args to pass the function
	 * @param listeners		Things that might want to listen to the status of this event (if any)
	 * @return				The event that was enqueued
	 */
	public LuaInvocation enqueueFunctionCall(String functionName, LuaValue[] args, ScriptEventStatusListener... listeners) {
		return enqueueFunctionCall(functionName, args, null, listeners);
	}

	/**
	 * @param codeBlock			A block of lua code to execute
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 * @return					The event that was enqueued
	 */
	public LuaInvocation enqueueCodeBlock(String codeBlock, ScriptEventStatusListener... listeners) {
		return enqueueCodeBlock(codeBlock, null, listeners);
	}

	/**
	 * @param codeBlock			A block of lua code to execute
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 * @return					The event that was enqueued
	 */
	public LuaInvocation enqueueCodeBlock(File codeBlock, ScriptEventStatusListener... listeners) {
		return enqueueCodeBlock(codeBlock, null, listeners);
	}
	
	/**
	 * Enqueues a lua function call
	 * 
	 * @param functionName		Name of the function to call
	 * @param args				Args to pass the function
	 * @param coalescingGroup	A group to coalesce events into
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 * @return				The event that was enqueued
	 */
	public LuaInvocation enqueueFunctionCall(String functionName, LuaValue[] args, CoalescingGroup<LuaInvocation> coalescingGroup, ScriptEventStatusListener... listeners) {
		LuaInvocation event = new LuaInvocation(this, functionName, args);
		
		scriptQueue.enqueue(event, coalescingGroup, listeners);
		
		return event;
	}
	
	/**
	 * @param codeBlock			A block of lua code to execute
	 * @param coalescingGroup	A group to coalesce events into
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 * @return					The event that was enqueued
	 */
	public LuaInvocation enqueueCodeBlock(String codeBlock, CoalescingGroup<LuaInvocation> coalescingGroup, ScriptEventStatusListener... listeners) {
		LuaInvocation event = new LuaInvocation(this, codeBlock);
		
		scriptQueue.enqueue(event, coalescingGroup, listeners);
		
		return event;
	}
		

		
		/**
		 * @param codeBlock			A block of lua code to execute
		 * @param coalescingGroup	A group to coalesce events into
		 * @param listeners			Things that might want to listen to the status of this event (if any)
		 * @return					The event that was enqueued
		 */
		public LuaInvocation enqueueCodeBlock(File codeBlock, CoalescingGroup<LuaInvocation> coalescingGroup, ScriptEventStatusListener... listeners) {
			LuaInvocation script;
			try {
				// May need to append newline to left string argument in accumulator function.
				script = new LuaInvocation(this,
						new BufferedReader(new FileReader(codeBlock)).lines()
						.reduce("", (a, b) -> a + "\n" + b));
			}
			catch (FileNotFoundException fileNotFound) {
				// TODO: Consider changing contract of method to return an Optional<LuaScript> or have it throw an exception
				script = new LuaInvocation(this, "");
			}
			scriptQueue.enqueue(script, coalescingGroup, listeners);
			
			return script;
	}

	public void addOutputEventListener(Consumer<String> fn) {
			outputEventListeners.add(fn);
	}

	public String getOutput() {
		try { bufferedOutputStream.flush(); }
		catch (IOException io) { }
		return outputStream.toString();
	}

	public class PrintfFunction extends VarArgFunction {
		@Override public LuaValue invoke(Varargs v) {
			String tofmt = v.arg1().checkjstring();
			List<Object> args = new ArrayList<>();
			for(int i = 2; i <= v.narg(); i++) {
				args.add(v.arg(i).tojstring());
			}
			String fmt = String.format(tofmt, args.toArray());
			try { bufferedOutputStream.write(fmt.getBytes()); }
			catch (IOException io) { }
			outputEventListeners.forEach(cn -> cn.accept(fmt));
			return LuaValue.NIL;
		}
	}

	public class PrintFunction extends VarArgFunction {
		@Override public LuaValue invoke(Varargs v) {
			String ans = v.tojstring();
			try { bufferedOutputStream.write(ans.getBytes()); }
			catch (IOException io) { }
			outputEventListeners.forEach(cn -> cn.accept(ans));
			return LuaValue.NIL;
		}
	}
	
	public class SleepFunction extends VarArgFunction {
		@Override public LuaValue invoke(Varargs v) {
			double sleeptime = v.optdouble(1, 1.0);
			
			try {
				Thread.sleep((long)(1000 * sleeptime));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return LuaValue.NIL;
		}
	}

	public PrintfFunction getPrintfFunction() {
		return new PrintfFunction();
	}

	public PrintFunction getPrintFunction() {
		return new PrintFunction();
	}

	public LuaValue getSleepFunction() {
		return new SleepFunction();
	}

	public void update(float dt) {
		scriptQueue.update(dt);
	}
}
