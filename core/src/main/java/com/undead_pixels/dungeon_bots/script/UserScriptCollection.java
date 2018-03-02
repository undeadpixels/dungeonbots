package com.undead_pixels.dungeon_bots.script;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A collection of UserScripts
 *
 */
public class UserScriptCollection implements Iterable<UserScript> {
	
	private HashMap<String, UserScript> storage = new HashMap<>();

	public void add(UserScript script) {
		storage.put(script.name, script);
		// TODO - this doesn't handle the editing of names
	}
	
	public UserScript get(String name) {
		return storage.getOrDefault(name, null);
	}

	public void clear() {
		storage.clear();
	}

	@Override
	public Iterator<UserScript> iterator() {
		return storage.values().iterator();
	}
}
