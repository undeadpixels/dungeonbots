package com.undead_pixels.dungeon_bots.script;

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

	public boolean onWhitelist(String id) {
		return whitelist.contains(id);
	}
}
