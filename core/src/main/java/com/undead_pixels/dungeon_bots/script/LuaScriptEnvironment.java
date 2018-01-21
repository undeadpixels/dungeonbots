package com.undead_pixels.dungeon_bots.script;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A LuaScriptEnvironment is a factory for creating a Sandbox of globals and methods that can be used to invoke
 * LuaScripts.
 */
public class LuaScriptEnvironment {

    private Globals globals;

    /**
     * An enumeration of different default Lua Globals that may be useful in different circumstances.
     */
    public enum Sandbox {
        Debug(JsePlatform.debugGlobals()),
        Default(JsePlatform.standardGlobals()),
        Limited(new Globals());

        public final Globals globals;

        Sandbox(Globals globals) {
            this.globals = globals;
        }
    }

    /**
     * Initializes a LuaScriptEnvironment using JsePlatform.standardGloabls() as the Globals
     */
    public LuaScriptEnvironment() {
        globals = Sandbox.Default.globals;
    }

    /**
     * Creates a new LuaScriptEnvironment using different enumerated default Global environments specified by the Sandbox parameter
     * @param sandbox An enumeration of different default Global environment types to use for the Script environment
     */
    public LuaScriptEnvironment(Sandbox sandbox) {
        globals = sandbox.globals;
    }

    /**
     * Initializes a new LuaScriptEnvironment adding the argument LuaValues as source files to the Global environment
     * @param args Source Lua libraries that will be appended to the Global environment table
     */
    public LuaScriptEnvironment(LuaValue... args) {
        globals = new Globals();
        Stream.of(args).forEach(val -> globals.load(val));
    }

	/**
	 * Inits a LuaScriptEnvironment using the argument Globals parameter
	 * @param globals
	 */
	public LuaScriptEnvironment(Globals globals) {
		this.globals = globals;
	}

	/**
	 * Adds a Collection of LuaBindings to the Global Environment for the LuaScriptEnvironment.
	 * @param bindings A collection of LuaBindings to append to the Global Environment
	 * @return The modified LuaScriptEnvironment
	 */
	public LuaScriptEnvironment add(Collection<LuaBinding> bindings) {
        for(LuaBinding binding : bindings)
            globals.set(binding.bindTo, binding.luaValue);
        return this;
    }

	/**
	 * Variadic method for adding the argument LuaBindings to the LuaScriptEnvironment. Essentially an overload of
	 * the add(Collection:LuaBinding) method
	 * @param bindings A variable number of LuaBinding parameters
	 * @return The modified LuaScriptEnvironment
	 */
	public LuaScriptEnvironment add(LuaBinding... bindings) {
        return add(Stream.of(bindings).collect(Collectors.toList()));
    }

    /**
     * Initializes a LuaScript using a file as the source script to run.
     * @param file A file that corresponds to the source script.
     * @return A LuaScript that is invoked using the current LuaScriptEnvironment
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
     * Creates a new LuaScript using the argument string as the source script to run.
     * @param script The source script to invoke
     * @return A LuaScript that is invoked using the current LuaScriptEnvironment
     */
    public LuaScript script(String script) {
        return new LuaScript(this, script);
    }

    /**
     * Creates a new LuaScript using the argument string as the script source, and starts the LuaScript
     * @param script The source script to invoke
     * @return A LuaScript that has been started with .start()
     */
    public LuaScript init(String script) {
        return this.script(script).start();
    }

    /**
     * Accessor for the Globals for the LuaScriptEnvironment
     * @return The Globals for the LuaScriptEnvironment
     */
    public Globals getGlobals() {
        return globals;
    }

    /**
     * Sets the Globals of the current LuaScriptEnvironment
     * @param globals The globals to use to set the LuaScriptEnvironment Globals with
     */
    public void setGlobals(Globals globals) {
        this.globals = globals;
    }
}
