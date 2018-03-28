package com.undead_pixels.dungeon_bots.script.security;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import com.undead_pixels.dungeon_bots.script.proxy.LuaBinding;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stewart Charles, Kevin Parker
 * @version 2/1/2018
 * Creates a Whitelist of allowed callable Methods that is unique to the caller
 * and the method.
 */
public class Whitelist implements GetLuaFacade, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, SecurityLevel> whitelist;
	private transient LuaValue luaValue;

	public Whitelist() {
		this.whitelist = new HashMap<>();
	}

	/**
	 * Adds the argument objects default Whitelist generated from the getWhitelist method<br>
	 * implemented by the GetLuaFacade interface by default.<br>
	 * @param bindables A variadic list of elements implementing the GetLuaFacade interface
	 * @param <T> A Type that implements GetLuaFacade
	 * @return The source Whitelist
	 */
	@SafeVarargs
	public final <T extends GetLuaFacade> Whitelist addAutoLevelsForBindables(final T... bindables) {
		for(T t : bindables) {
			Whitelist w = t.getDefaultWhitelist();
			this.addWhitelistNoReplace(w);
		}
		return this;
	}
	
	@SafeVarargs
	public final <T extends GetLuaFacade> Whitelist addAutoLevelsForBindables(final Class<? extends GetLuaFacade>... bindables) {
		for(Class<? extends GetLuaFacade> t : bindables) {
			this.addWhitelistNoReplace(GetLuaFacade.getWhitelist(t));
		}
		return this;
	}

	/**
	 * Removes the argument method to the Whitelist with an ID associated with the caller.<br>
	 * 
	 * @param method
	 * @return	this
	 */
	public <T extends GetLuaFacade> Whitelist remove(final Method method) {
		return setLevel(method, SecurityLevel.AUTHOR);
	}

	
	/**
	 * Adds the argument method to the Whitelist with a given security level.<br>
	 * Use this to Whitelist/blacklist a given method.<br>
	 * 
	 * @param method The target method
	 * @param securityLevel The target security level
	 * @return The source Whitelist
	 */
	public Whitelist setLevel(final Method method, SecurityLevel securityLevel) {
		return setLevel(LuaReflection.genId(method), securityLevel);
	}

	/**
	 * Adds the argument method to the Whitelist with a given security level.<br>
	 * Use this to Whitelist/blacklist a given method.<br>
	 * 
	 * @param bindId The target method ID
	 * @param securityLevel The target security level
	 * @return The source Whitelist
	 */
	public Whitelist setLevel(final String bindId, SecurityLevel securityLevel) {
		whitelist.put(bindId, securityLevel);
		return this;
	}

	/**
	 * Query if the given method is on the source Whitelist associated with the specified caller
	 * @param caller The caller that invokes the specified method. Can be null if a static method.
	 * @param m The target method.
	 * @return True if found on the whitelist
	 */
	@SuppressWarnings("unchecked")
	public <T extends GetLuaFacade> SecurityLevel getLevel(final Method m) {
		String bindID = LuaReflection.genId(m);
		SecurityLevel level = whitelist.getOrDefault(bindID, null);
		
		if(level == null) {
			Class<?> uncastedClass = m.getDeclaringClass();
			try {
				if(GetLuaFacade.class.isAssignableFrom(uncastedClass)) {
					addAutoLevelsForBindables((Class<? extends GetLuaFacade>)uncastedClass);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			return whitelist.getOrDefault(bindID, SecurityLevel.DEBUG);
		}
		return level;
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
	 * 
	 * usage:
	 * 
	 * setLevel("com.undead_pixels.dungeon_bots.entites.Actor:up", "general")
	 * 
	 * 
	 * 
	 * // <pre>{@code
	 * //     -- Whitelists functions named up, down, left and right that belong to the player entity if found
	 * //     whitelist.allow(player, "up", "down", "left", "right")
	 * //
	 * //     -- Whitelists all functions for the player entity
	 * //     whitelist.allow(player)
	 * //     }</pre>
	 * @param obj
	 * @param toAllow
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Whitelist setLevel(LuaValue obj, LuaValue methodName, LuaValue permissionLevel) {
		GetLuaFacade o = (GetLuaFacade) obj.checktable().get("this").checkuserdata(GetLuaFacade.class);
		Method method = LuaReflection.getMethodWithName(o, methodName.checkjstring()).get();
		
		SecurityLevel securityLevel = SecurityLevel.DEBUG;
		
		switch(permissionLevel.checkjstring().toLowerCase()) {
		case "author":
			securityLevel = SecurityLevel.AUTHOR;
			break;
		case "entity":
			securityLevel = SecurityLevel.ENTITY;
			break;
		case "team":
			securityLevel = SecurityLevel.TEAM;
			break;
		case "default":
			securityLevel = SecurityLevel.DEFAULT;
			break;
		case "none":
		case "general":
			securityLevel = SecurityLevel.NONE;
			break;
			
		default:
			// TODO - throw some exception?
			return this;
		}
		
		return this.setLevel(method, securityLevel);
	}

	private Whitelist addWhitelistNoReplace(Whitelist w) {

		for(String methodID : w.whitelist.keySet()) {
			SecurityLevel newLevel = w.whitelist.get(methodID);
			whitelist.putIfAbsent(methodID, newLevel);
		}

		return this;
	}
}
