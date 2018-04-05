package com.undead_pixels.dungeon_bots.script;
import com.undead_pixels.dungeon_bots.LuaDoc;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.queueing.CoalescingGroup;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.events.ScriptEventQueue;
import com.undead_pixels.dungeon_bots.script.proxy.LuaBinding;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.*;
/**
 * @author Stewart Charles
 * @version 2/1/2018
 * A LuaSandbox is a factory for creating a Sandbox of globals and methods that can be used to invoke
 * LuaScripts. A LuaSandbox is essentially a collection of allowed Lua functions.<br>
 * LuaSandbox's manage setting up the SecurityContext for the invoked LuaScripts when they are called.
 */
public final class LuaSandbox implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int id = 0;
	
	private final UserScriptCollection scripts;
	private final Globals globals;
	final Globals invokerGlobals = JsePlatform.debugGlobals();
	private final SecurityContext securityContext; // TODO - init this
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
	private final ScriptEventQueue scriptQueue = new ScriptEventQueue(this);
	private final List<Consumer<String>> outputEventListeners = new ArrayList<>();
	private final ThreadGroup threadGroup = new ThreadGroup("lua-"+(id++));
	private final HashMap<String, HashSet<LuaValue>> eventListeners = new HashMap<>();

	/**
	 * Initializes a LuaSandbox using JsePlatform.standardGloabls() as the Globals
	 */
	@Deprecated
	public LuaSandbox() {
		this(SecurityLevel.DEFAULT);
		registerGlobalFunctions();
	}

	/**
	 * Creates a new LuaSandbox using different enumerated default Global environments specified by the Sandbox parameter
	 * @param securityLevel An enumeration of different default Global environment types to use for the Script environment
	 */
	@Deprecated
	public LuaSandbox(SecurityLevel securityLevel) {
		this.securityContext = new SecurityContext(new Whitelist(), securityLevel, null, null, TeamFlavor.NONE);
		this.globals = securityContext.getSecurityLevel().getGlobals();
		this.scripts = new UserScriptCollection();
		registerGlobalFunctions();
	}

	public LuaSandbox(Entity e) {
		this.securityContext = new SecurityContext(e);
		this.globals = securityContext.getSecurityLevel().getGlobals();
		this.scripts = e.getScripts();
		registerGlobalFunctions();
	}
	public LuaSandbox(World w) {
		this.securityContext = new SecurityContext(w);
		this.globals = securityContext.getSecurityLevel().getGlobals();
		this.scripts = w.getScripts();
		registerGlobalFunctions();
	}
	
	private void registerGlobalFunctions() {
		globals.set("print", new PrintFunction());
		globals.set("printf", new PrintfFunction());
		globals.set("sleep", new SleepFunction());
		globals.set("require", new RequireFunction());
		globals.set("help", new HelpFunction());
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

	/**
	 * Adds the bindings of the argument collection of Bindable objects to the source LuaSandbox
	 * @param bindName The Name to bind the bindable to in the Lua Script environment
	 * @param bindable The Bindable to insert into the script environment
	 * @param <T> The type of the Bindable
	 * @return The invoked LuaSandbox
	 */
	public final <T extends GetLuaFacade> LuaSandbox  addBindable(String bindName, T bindable) {
		securityContext.getWhitelist().addAutoLevelsForBindables(bindable);
		add(new LuaBinding(bindName, bindable.getLuaValue()));
		return this;
	}

	/**
	 * Adds the static bindings of the argument Class
	 * @param clz The Class to add the static Bindings of
	 * @return The source LuaSandbox
	 */
	public final LuaSandbox addBindableClass(final Class<? extends GetLuaFacade> clz) {
		securityContext.getWhitelist().addAutoLevelsForBindables(clz);
		add(LuaProxyFactory.getBindings(clz));
		return this;
	}

	public final LuaSandbox addBindableClasses(final List<Class<? extends GetLuaFacade>> classes) {
		classes.forEach(clz -> addBindableClass(clz));
		return this;
	}

	/**
	 * Variadic method for adding the argument LuaBindings to the LuaSandbox. Essentially an overload of
	 * the add(Collection:LuaBinding) method
	 * @param bindings A variable number of LuaBinding parameters
	 * @return The modified LuaSandbox
	 */
	public LuaSandbox add (LuaBinding... bindings) {
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
	 * @return
	 */
	public LuaInvocation init() {
		UserScript script = this.scripts.get("init");
		assert script != null;
		LuaInvocation ret = this.enqueueCodeBlock(script.code);
		scriptQueue.update(0.f);
		
		return ret;
	}
	
	/**
	 * Accessor for the Globals for the LuaSandbox
	 * 
	 * @return The Globals for the LuaSandbox
	 */
	public Globals getGlobals () {
		return globals;
	}

	/**
	 * Get the Whitelist of the LuaSandbox
	 * @return
	 */
	public Whitelist getWhitelist() {
		return securityContext.getWhitelist();
	}

	/**
	 * Get the SecurityLevel of the LuaSandbox
	 * @return
	 */
	public SecurityLevel getSecurityLevel() {
		return securityContext.getSecurityLevel();
	}
	
	public ScriptEventQueue getQueue() {
		return scriptQueue;
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
		 * @param file				A file containing lua code to execute
		 * @param coalescingGroup	A group to coalesce events into
		 * @param listeners			Things that might want to listen to the status of this event (if any)
		 * @return					The event that was enqueued
		 */
		public LuaInvocation enqueueCodeBlock(File file, CoalescingGroup<LuaInvocation> coalescingGroup, ScriptEventStatusListener... listeners) {
			UserScript codeBlock = new UserScript("init", file);
			LuaInvocation script = new LuaInvocation(this, codeBlock);
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
	
	private void doPrint(String str) {
		System.out.println("print: "+str);
		try { bufferedOutputStream.write(str.getBytes()); }
		catch (IOException io) { }
		outputEventListeners.forEach(cn -> cn.accept(str));
	}

	public class PrintfFunction extends VarArgFunction {
		@Override public LuaValue invoke(Varargs v) {
			String tofmt = v.arg1().checkjstring();
			List<Object> args = new ArrayList<>();
			for(int i = 2; i <= v.narg(); i++) {
				args.add(CoerceLuaToJava.coerce(v.arg(i), Object.class));
			}
			String fmt = String.format(tofmt, args.toArray());
			doPrint(fmt);
			return LuaValue.NIL;
		}
	}

	public class PrintFunction extends VarArgFunction {
		@Override public LuaValue invoke(Varargs v) {
			String ans = v.tojstring();
			doPrint(ans);
			return LuaValue.NIL;
		}
	}
	
	public class SleepFunction extends VarArgFunction {
		@Override public LuaValue invoke(Varargs v) {
			double sleeptime = v.optdouble(1, 1.0);
			
			LuaInvocation currentInvoke = getQueue().getCurrent();
			currentInvoke.safeSleep((long) (1000 * sleeptime));
			
			return LuaValue.NIL;
		}
	}

	public class RequireFunction extends OneArgFunction {

		@Override
		public LuaValue call(LuaValue required) {
			return Optional.ofNullable(scripts.get(required.checkjstring()))
					.map(script ->
							init(script.code)
									.join()
									.getResults()
									.map(Varargs::arg1)
									.orElse(LuaValue.NIL))
					.orElse(LuaValue.NIL);
		}
	}

	public class HelpFunction extends VarArgFunction {
		@Override
		public LuaValue invoke(Varargs v) {
			doPrint(
					luaValueStream(v)
						.filter(lv -> lv.istable())
						.map(lv -> lv.checktable())
						.filter(tbl -> tbl.get("this").isuserdata() || tbl.get("class").isuserdata())
						.map(tbl -> tbl.get("this") == LuaValue.NIL ?
								(Class<?>)tbl.get("class").checkuserdata(Class.class) :
								tbl.get("this").checkuserdata().getClass())
						.map(obj -> LuaDoc.docClassToString(obj))
						.reduce("", (a, b) -> a + b));
			return LuaValue.NIL;
		}
	}

	private static Stream<LuaValue> luaValueStream(final Varargs v) {
		List<LuaValue> ans = new ArrayList<>();
		for(int i = 1; i <= v.narg(); i++) {
			ans.add(v.arg(i));
		}
		return ans.stream();
	}

	public void update(float dt) { scriptQueue.update(dt); }

	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	public ThreadGroup getThreadGroup() {
		return threadGroup;
	}

	/**
	 * Registers an event type and allows this sandbox to request to listen to it
	 * 
	 * @param eventName		Something of the form FOO_BAR_BLAH,
	 * 						which would create a function in the lua environment named registerFooBarBlahListener
	 */
	public synchronized void registerEventType(String eventName) {
		eventListeners.put(eventName, new HashSet<LuaValue>());
		
		
		OneArgFunction registerEventListenerFunction = new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(!arg.isfunction()) {
					throw new LuaError("Expected a function");
				}
				eventListeners.get(eventName).add(arg);
				return LuaValue.NIL;
			}
		};
		
		// Make the name of the function: FOO_BAR_BLAH -> registerFooBarBlahListener
		boolean shouldUpper = true;
		String registerEventListenerFunctionName = "";
		String eventNameLower = eventName.toLowerCase();
		for(int i = 0; i < eventNameLower.length(); i++) {
			char c = eventNameLower.charAt(i);
			if(c == '_') {
				shouldUpper = true;
				continue;
			}
			
			if(shouldUpper) {
				registerEventListenerFunctionName += (""+c).toUpperCase();
				shouldUpper = false;
			} else {
				registerEventListenerFunctionName += c;
			}
		}
		
		registerEventListenerFunctionName = "register" + registerEventListenerFunctionName + "Listener";
		
		globals.set(registerEventListenerFunctionName, registerEventListenerFunction);
	}

	public synchronized LuaInvocation fireEvent(String eventName, LuaValue... args) {
		return fireEvent(eventName, null, args);
	}
	public synchronized LuaInvocation fireEvent(String eventName, CoalescingGroup<LuaInvocation> coalescingGroup, LuaValue... args) {
		LuaInvocation invocation = new LuaInvocation(this, eventListeners.get(eventName), args);
		scriptQueue.enqueue(invocation, coalescingGroup);
		
		return invocation;
	}

	/**
	 * @param trigger
	 */
	public void safeWaitUntil (Supplier<Boolean> trigger) {
		LuaInvocation currentInvoke = this.getQueue().getCurrent();
		while(trigger.get() == false) {
			currentInvoke.safeSleep(5);
		}
	}

}
