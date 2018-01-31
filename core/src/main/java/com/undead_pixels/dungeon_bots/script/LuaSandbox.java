package com.undead_pixels.dungeon_bots.script;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.proxy.LuaBinding;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import org.luaj.vm2.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
	public LuaSandbox add(Stream<LuaBinding> bindings) {
        bindings.forEach(binding ->
				globals.set(binding.bindTo, binding.luaValue));
        return this;
    }

    @SafeVarargs
    public final <T extends GetBindable> LuaSandbox  addBindable(T... bindable) {
		whitelist.add(securityLevel, bindable);
		add(Stream.of(bindable)
				.map(GetBindable::getLuaBinding));
		return this;
	}



	public <T extends GetBindable> LuaSandbox addBindableClass(Class<T> clz) {
		whitelist.add(securityLevel, clz);
		LuaBinding b = LuaProxyFactory.getBindings(clz);
		add(b);
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
     * Initializes a LuaScript using a file as the source sandbox to run.
     * @param file A file that corresponds to the source sandbox.
     * @return A LuaScript that is invoked using the current LuaSandbox
     */
    public LuaScript script(File file) {
    	try {
			BufferedReader fr = new BufferedReader(new FileReader(file));
			// May need to append newline to left string argument in accumulator function.
			return script(fr.lines().reduce("", (a, b) -> a + "\n" + b));
		}
		catch (FileNotFoundException fileNotFound) {
    		// TODO: Consider changing contract of method to return an Optional<LuaScript> or have it throw an exception
    		return script("");
		}
    }

    /**
     * Creates a new LuaScript using the argument string as the source sandbox to run.
     * @param script The source sandbox to invoke
     * @return A LuaScript that is invoked using the current LuaSandbox
     */
    public LuaScript script(String script) {
        return new LuaScript(this, script);
    }

    /**
     * Creates a new LuaScript using the argument string as the sandbox source, and starts the LuaScript
     * @param script The source sandbox to invoke
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

	public static String id(Object o, Method m) {
		return o.hashCode() + m.toGenericString();
	}

	public static String staticId(Method m) {
		return m.toGenericString();
	}

	public static String id(Object o, Field f) {
		return o.hashCode() + f.toGenericString();
	}

	public SecurityLevel getSecurityLevel() {
		return securityLevel;
	}
}
