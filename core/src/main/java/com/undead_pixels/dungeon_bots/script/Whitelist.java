package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import org.luaj.vm2.LuaValue;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Whitelist implements GetBindable {
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

	public <T extends GetBindable> Whitelist add(T bindable) {
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

	public <T extends GetBindable> Whitelist add(final T caller, final Method m) {
		whitelist.add(LuaReflection.genId(caller,m));
		return this;
	}

	public <T extends GetBindable> Whitelist remove(final T caller, final Method m) {
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

	public <T extends GetBindable> Whitelist add(final SecurityLevel securityLevel, T... args) {
		return addWhitelists(Stream.of(args).map(val -> val.getWhitelist(securityLevel)));
	}

	public <T extends GetBindable> Whitelist add(final SecurityLevel securityLevel, final Class<T> arg) {
		whitelist.addAll(GetBindable.getWhitelist(arg, securityLevel).whitelist);
		return this;
	}

	public boolean onWhitelist(String bindId) {
		return whitelist.contains(bindId);
	}

	public <T extends GetBindable> boolean  onWhitelist(T caller, Method m) {
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

}
