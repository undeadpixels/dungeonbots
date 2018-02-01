package com.undead_pixels.dungeon_bots.script.security;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates a Whitelist of allowed callable Methods that is unique to the caller
 * and the method.
 */
public class Whitelist implements GetLuaFacade {
	private Set<String> whitelist;
	private LuaValue luaValue;

	public Whitelist() {
		this.whitelist = new HashSet<>();
	}

	/**
	 * Add the Argument Whitelist ID's to the Whitelist
	 * @param args
	 * @return
	 */
	public Whitelist addId(final String... args) {
		return addId(Stream.of(args));
	}

	/**
	 * Remove the Argument Whitelist ID's to the Whitelist
	 * @param args
	 * @return
	 */
	public Whitelist removeId(final String... args) {
		return removeId(Stream.of(args));
	}

	/**
	 * Adds the argument ID's to the source Whitelist
	 * @param args A Stream of String ID's to add
	 * @return The source Whitelist
	 */
	public  Whitelist addId(final Stream<String> args) {
		whitelist.addAll(args.collect(Collectors.toList()));
		return this;
	}

	/**
	 * Removes the argument ID's from the source Whitelist
	 * @param args A Stream of String ID's to remove
	 * @return The source Whitelist
	 */
	public Whitelist removeId(final Stream<String> args) {
		whitelist.removeAll(args.collect(Collectors.toList()));
		return this;
	}

	/**
	 * Adds the argument objects default Whitelist generated from the getWhitelist method<br>
	 * implemented by the GetLuaFacade interface by default.<br>
	 * @param bindables A variadic list of elements implementing the GetLuaFacade interface
	 * @param <T> A Type that implements GetLuaFacade
	 * @return The source Whitelist
	 */
	@SafeVarargs
	public final <T extends GetLuaFacade> Whitelist add(final T... bindables) {
		Stream.of(bindables).forEach(bindable -> addWhitelists(bindable.getWhitelist()));
		return this;
	}

	/**
	 * Adds the argument method to the Whitelist with an ID associated with the caller.<br>
	 * Use this to Whitelist a caller invoking a given method.<br>
	 * @param caller The caller object
	 * @param m The target method
	 * @param <T> A type that implements GetLuaFacade
	 * @return The source Whitelist
	 */
	public <T extends GetLuaFacade> Whitelist add(final T caller, final Method m) {
		whitelist.add(LuaReflection.genId(caller,m));
		return this;
	}

	/**
	 * Removes the argument method to the Whitelist with an ID associated with the caller.<br>
	 * Use this to Whitelist a caller invoking a given method.<br>
	 * This disallows a specific caller from invoking a method.<br>
	 * @param caller
	 * @param m
	 * @param <T>
	 * @return
	 */
	public <T extends GetLuaFacade> Whitelist remove(final T caller, final Method m) {
		whitelist.remove(LuaReflection.genId(caller, m));
		return this;
	}

	/**
	 * Adds the argument elements Whitelists to the source Whitelist.
	 * @param securityLevel The Security Level of the Whitelists to retrieve <br>
	 *                         when calling the GetLuaFacade.getWhitlist function
	 * @param args A variadic array of elements implementing the GetLuaFacade interface
	 * @param <T> A type that implements the GetLuaFacade interface
	 * @return The source Whitelist
	 */
	public <T extends GetLuaFacade> Whitelist add(final SecurityLevel securityLevel, final T... args) {
		return addWhitelists(Stream.of(args).map(val -> val.getWhitelist(securityLevel)));
	}

	/**
	 * Adds the whitelist of the argument Class to the source Whitelist
	 * @param securityLevel The security level to invoke when acquiring the Whitelist of the argument class
	 * @param arg The argument class
	 * @param <T> A Type that implements GetLuaFacade to obtain the Whitelist
	 * @return The source Whitelist
	 */
	public <T extends GetLuaFacade> Whitelist add(final SecurityLevel securityLevel, final Class<T> arg) {
		whitelist.addAll(GetLuaFacade.getWhitelist(arg, securityLevel).whitelist);
		return this;
	}

	/**
	 * Query if a specific id is in the source Whitelist
	 * @param bindId The String id to query
	 * @return True if found on the whitelist
	 */
	public boolean onWhitelist(final String bindId) {
		return whitelist.contains(bindId);
	}

	/**
	 * Query if the given method is on the source Whitelist associated with the specified caller
	 * @param caller The caller that invokes the specified method. Can be null if a static method.
	 * @param m The target method.
	 * @param <T> A Type that implements GetLuaFacade
	 * @return True if found on the whitelist
	 */
	public <T extends GetLuaFacade> boolean onWhitelist(final T caller, final Method m) {
		return onWhitelist(LuaReflection.genId(caller,m));
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int getId() {
		return this.hashCode();
	}

	/**
	 * @return
	 */
	@Override
	public String getName() {
		return "whitelist";
	}

	/**
	 * Creates or return an existing LuaValue facade of the Whitelist
	 * @return
	 */
	@Override
	public LuaValue getLuaValue() {
		if(this.luaValue == null)
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		return this.luaValue;
	}

	/**
	 * Binding to Lua code that allows Authors to add things to the whitelist.<br>
	 * <pre>{@code
	 *     -- Whitelists functions named up, down, left and right that belong to the player entity if found
	 *     whitelist.allow(player, "up", "down", "left", "right")
	 *
	 *     -- Whitelists all functions for the player entity
	 *     whitelist.allow(player)
	 *     }</pre>
	 * @param varargs
	 */
	@Bind(SecurityLevel.AUTHOR)
	public void allow(Varargs varargs) {
		final int SIZE = varargs.narg();
		assert SIZE > 0;
		LuaTable tbl = varargs.checktable(1);
		GetLuaFacade val = (GetLuaFacade) tbl.checkuserdata(1, GetLuaFacade.class);
		if(SIZE == 1) {
			addWhitelists(val.getWhitelist());
		}
		else {
			List<String> methodNames = new ArrayList<>();
			for(int i = 2; i < SIZE; i++) {
				try { methodNames.add(varargs.arg(i).checkjstring()); }
				catch (Exception e) { }
			}
			methodNames.forEach(name ->
				LuaReflection.getMethodWithName(val, name)
						.ifPresent(m -> whitelist.add(LuaReflection.genId(val, m))));
		}
	}

	private Whitelist addWhitelists(Whitelist... w) {
		return addWhitelists(Stream.of(w));
	}

	private Whitelist addWhitelists(Stream<Whitelist> w) {
		w.forEach(val -> whitelist.addAll(val.whitelist));
		return this;
	}
}
