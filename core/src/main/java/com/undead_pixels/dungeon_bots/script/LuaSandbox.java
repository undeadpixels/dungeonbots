package com.undead_pixels.dungeon_bots.script;
import com.undead_pixels.dungeon_bots.script.interfaces.LuaReflection;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.luaj.vm2.*;
import java.io.*;
import java.util.Collection;
import java.util.stream.*;

/**
 * A LuaSandbox is a factory for creating a Sandbox of globals and methods that can be used to invoke
 * LuaScripts.
 */
public class LuaSandbox {

    private Globals globals;

    private final Whitelist whitelist = new Whitelist();
    private final SecurityLevel securityLevel;

	/**
     * Initializes a LuaSandbox using JsePlatform.standardGloabls() as the Globals
     */
    public LuaSandbox() {
    	securityLevel = SecurityLevel.AUTHOR;
    	globals = securityLevel.globals;
    }

    /**
     * Creates a new LuaSandbox using different enumerated default Global environments specified by the Sandbox parameter
     * @param securityLevel An enumeration of different default Global environment types to use for the Script environment
     */
    public LuaSandbox(SecurityLevel securityLevel) {
    	this.securityLevel = securityLevel;
        globals = securityLevel.globals;
    }

	/**
	 * Inits a LuaSandbox using the argument Globals parameter
	 * @param globals
	 */
	public LuaSandbox(Globals globals) {
		this.securityLevel = SecurityLevel.AUTHOR;
		this.globals = globals;
	}

	/**
	 * Adds a Collection of LuaBindings to the Global Environment for the LuaSandbox.
	 * @param bindings A collection of LuaBindings to append to the Global Environment
	 * @return The modified LuaSandbox
	 */
	public LuaSandbox add(Collection<LuaBinding> bindings) {
        for(LuaBinding binding : bindings)
            globals.set(binding.bindTo, binding.luaValue);
        return this;
    }

	/**
	 * Creates a LuaValue proxy for each object and binds to the LuaValue to the Sandbox Environment.
	 * Does not populate Whitelist.
	 * @param toAdd A List of Objects implementing the Scriptable interface.
	 * @param <T> A generic type implementing Scriptable
	 * @return Returns the LuaSandbox
	 */
    @SafeVarargs
	public final <T extends Scriptable & LuaReflection> LuaSandbox restrictiveAdd(T... toAdd) {
		add(Stream.of(toAdd)
				.map(src -> LuaProxyFactory.getBindings(src, whitelist, securityLevel))
				.collect(Collectors.toList()));
		return this;
	}

	/**Creates a LuaValue proxy for each object and binds to the LuaValue to the Sandbox Environment.
	 * Uses reflection to populate an initial whitelist that contains all of the available methods of the argument objects.
	 * @param toAdd A List of Objects implementing the Scriptable interface.
	 * @param <T> A generic type implementing Scriptable
	 * @return Returns the LuaSandbox
	 */
	@SafeVarargs
    public final <T extends LuaReflection & Scriptable> LuaSandbox permissiveAdd(T... toAdd) {
		whitelist.add(toAdd);
		add(Stream.of(toAdd)
				.map(src -> LuaProxyFactory.getBindings(src, whitelist, securityLevel))
				.collect(Collectors.toList()));
		return this;
	}

	/**
	 * Variadic method for adding the argument LuaBindings to the LuaSandbox. Essentially an overload of
	 * the add(Collection:LuaBinding) method
	 * @param bindings A variable number of LuaBinding parameters
	 * @return The modified LuaSandbox
	 */
	public LuaSandbox add(LuaBinding... bindings) {
        return add(Stream.of(bindings).collect(Collectors.toList()));
    }

    /**
     * Initializes a LuaScript using a file as the source scriptEnv to run.
     * @param file A file that corresponds to the source scriptEnv.
     * @return A LuaScript that is invoked using the current LuaSandbox
     */
    public LuaScript script(File file) {
    	try {
			BufferedReader fr = new BufferedReader(new FileReader(file));
			// May need to append newline to left string argument in accumulator function.
			return script(fr.lines().reduce("", String::concat));
		}
		catch (FileNotFoundException fileNotFound) {
    		// TODO: Consider changing contract of method to return an Optional<LuaScript> or have it throw an exception
    		return script("");
		}
    }

    /**
     * Creates a new LuaScript using the argument string as the source scriptEnv to run.
     * @param script The source scriptEnv to invoke
     * @return A LuaScript that is invoked using the current LuaSandbox
     */
    public LuaScript script(String script) {
        return new LuaScript(this, script);
    }

    /**
     * Creates a new LuaScript using the argument string as the scriptEnv source, and starts the LuaScript
     * @param script The source scriptEnv to invoke
     * @return A LuaScript that has been started with .start()
     */
    public LuaScript init(String script) {
        return this.script(script).start();
    }

    /**
     * Accessor for the Globals for the LuaSandbox
     * @return The Globals for the LuaSandbox
     */
    public Globals getGlobals() {
        return globals;
    }

    /**
     * Sets the Globals of the current LuaSandbox
     * @param globals The globals to use to set the LuaSandbox Globals with
     */
    public void setGlobals(Globals globals) {
        this.globals = globals;
    }

	/**
	 *
	 * @return
	 */
	public Whitelist getWhitelist() {
		return whitelist;
	}
}
