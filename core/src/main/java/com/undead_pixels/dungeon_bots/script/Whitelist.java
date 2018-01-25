package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.interfaces.LuaReflection;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Whitelist {
	private Set<String> whitelist;

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

	public Whitelist addTo(Collection<String> args) {
		whitelist.addAll(args);
		return this;
	}

	public Whitelist removeFrom(Collection<String> args) {
		whitelist.removeAll(args);
		return this;
	}

	public <T extends LuaReflection> Whitelist add(T caller, Method m) {
		whitelist.add(caller.genId(m));
		return this;
	}

	public <T extends LuaReflection> Whitelist remove(T caller, Method m) {
		whitelist.remove(caller.genId(m));
		return this;
	}

	public Whitelist addWhitelists(Collection<Whitelist> w) {
		w.forEach(val -> whitelist.addAll(val.whitelist));
		return this;
	}


	public Whitelist removeWhitelists(Collection<Whitelist> w) {
		w.forEach(val -> whitelist.removeAll(val.whitelist));
		return this;
	}

	public <T extends LuaReflection> Whitelist add(T... args) {
		return addWhitelists(Stream.of(args).map(LuaReflection::permissiveWhitelist).collect(Collectors.toList()));
	}

	public boolean onWhitelist(String bindId) {
		return whitelist.contains(bindId);
	}

	public <T extends LuaReflection> boolean  onWhitelist(T caller, Method m) {
		return whitelist.contains(caller.genId(m));
	}
}
