package com.undead_pixels.dungeon_bots.script;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.proxy.LuaBinding;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import org.luaj.vm2.*;
import java.io.*;
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
    private final Whitelist whitelist = new Whitelist();
	private SecurityLevel securityLevel;

	/**
     * Initializes a LuaSandbox using JsePlatform.standardGloabls() as the Globals
     */
    public LuaSandbox() {
    	this(SecurityLevel.AUTHOR);
    }

    public LuaSandbox setSecurityLevel(SecurityLevel securityLevel) {
    	this.securityLevel = securityLevel;
    	return this;
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
		this(SecurityLevel.AUTHOR, globals);
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

	/**
	 * Adds the bindings of the argument collection of Bindable objects to the source LuaSandbox
	 * @param bindable A Collection of Objects that implement the GetLuaFacade interface
	 * @param <T> A Type that implements the GetLuaFacade interface
	 * @return The source LuaSandbox
	 */
    @SafeVarargs
    public final <T extends GetLuaFacade> LuaSandbox  addBindable(T... bindable) {
		whitelist.add(securityLevel, bindable);
		add(Stream.of(bindable)
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
		Stream.of(clz).forEach(c -> {
			whitelist.add(securityLevel, c);
			add(LuaProxyFactory.getBindings(c));
		});
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
			// May need to append newline to left string argument in accumulator function.
			return script(new BufferedReader(new FileReader(file)).lines()
					.reduce("", (a, b) -> a + "\n" + b));
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

}
