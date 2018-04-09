package com.undead_pixels.dungeon_bots.script;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A collection of UserScripts
 *
 */
public class UserScriptCollection implements Iterable<UserScript>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<String, UserScript> storage;


	public UserScriptCollection() {
		storage = new HashMap<String, UserScript>();
	}


	public UserScriptCollection(UserScript[] scripts) {
		this();
		for (UserScript u : scripts)
			add(u.copy());
	}


	public synchronized void add(UserScript script) {
		storage.put(script.name, script);
		// TODO - this doesn't handle the editing of names
	}


	public synchronized UserScript get(String name) {
		return storage.getOrDefault(name, null);
	}


	public synchronized void remove(String name) {
		storage.remove(name);
	}


	public synchronized void clear() {
		storage.clear();
	}


	@Override
	public synchronized Iterator<UserScript> iterator() {
		return storage.values().iterator();
	}


	/**Returns an array of all contained UserScripts, sorted by name.*/
	public synchronized UserScript[] toArray() {
		UserScript[] result = storage.values().toArray(new UserScript[storage.values().size()]);
		Arrays.sort(result);
		return result;
	}


	/**
	 * @param other
	 */
	public synchronized void setTo (UserScriptCollection other) {
		storage.clear();
		storage.putAll(other.storage);
	}


	@Override
	public synchronized String toString () {
		StringBuilder ret = new StringBuilder();
		
		for(String name: storage.keySet()) {
			ret.append(storage.get(name).toString());
		}
		
		return ret.toString();
	}
}
