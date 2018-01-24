package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.interfaces.SecurityReflection;

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

	public Whitelist addTo(Object... args) {
		whitelist.addAll(Stream.of(args).map(Object::toString).collect(Collectors.toList()));
		return this;
	}

	public Whitelist removeFrom(Object... args) {
		whitelist.removeAll(Stream.of(args).map(Object::toString).collect(Collectors.toList()));
		return this;
	}

	public Whitelist add(Collection<Whitelist> w) {
		w.forEach(val -> whitelist.addAll(val.whitelist));
		return this;
	}

	public Whitelist remove(Collection<Whitelist> w) {
		w.forEach(val -> whitelist.removeAll(val.whitelist));
		return this;
	}

	public <T extends SecurityReflection> Whitelist add(T... args) {
		return add(Stream.of(args).map(SecurityReflection::generateWhitelist).collect(Collectors.toList()));
	}

	public boolean onWhitelist(String id) {
		return whitelist.contains(id);
	}
}
