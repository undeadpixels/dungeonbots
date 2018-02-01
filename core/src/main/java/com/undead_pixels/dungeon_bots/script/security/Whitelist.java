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

	public Whitelist addTo(String... args) {
		whitelist.addAll(Stream.of(args).collect(Collectors.toList()));
		return this;
	}

	public Whitelist removeFrom(String... args) {
		whitelist.removeAll(Stream.of(args).collect(Collectors.toList()));
		return this;
	}

	public <T extends GetLuaFacade> Whitelist add(T bindable) {
		addWhitelists(bindable.getWhitelist());
		return this;
	}

	public  Whitelist addTo(Stream<String> args) {
		whitelist.addAll(args.collect(Collectors.toList()));
		return this;
	}

	public Whitelist removeFrom(Stream<String> args) {
		whitelist.removeAll(args.collect(Collectors.toList()));
		return this;
	}

	public <T extends GetLuaFacade> Whitelist add(final T caller, final Method m) {
		whitelist.add(LuaReflection.genId(caller,m));
		return this;
	}

	public <T extends GetLuaFacade> Whitelist remove(final T caller, final Method m) {
		whitelist.remove(LuaReflection.genId(caller, m));
		return this;
	}

	public Whitelist addWhitelists(Stream<Whitelist> w) {
		w.forEach(val -> whitelist.addAll(val.whitelist));
		return this;
	}

	public Whitelist removeWhitelists(Stream<Whitelist> w) {
		w.forEach(val -> whitelist.removeAll(val.whitelist));
		return this;
	}

	public Whitelist addWhitelists(Whitelist... w) {
		return addWhitelists(Stream.of(w));
	}

	public <T extends GetLuaFacade> Whitelist add(final SecurityLevel securityLevel, T... args) {
		return addWhitelists(Stream.of(args).map(val -> val.getWhitelist(securityLevel)));
	}

	public <T extends GetLuaFacade> Whitelist add(final SecurityLevel securityLevel, final Class<T> arg) {
		whitelist.addAll(GetLuaFacade.getWhitelist(arg, securityLevel).whitelist);
		return this;
	}

	public boolean onWhitelist(String bindId) {
		return whitelist.contains(bindId);
	}

	public <T extends GetLuaFacade> boolean  onWhitelist(T caller, Method m) {
		return whitelist.contains(LuaReflection.genId(caller,m));
	}

	@Override
	public int getId() {
		return this.hashCode();
	}

	@Override
	public String getName() {
		return "whitelist";
	}

	@Override
	public LuaValue getLuaValue() {
		if(this.luaValue == null)
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		return this.luaValue;
	}

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

}
