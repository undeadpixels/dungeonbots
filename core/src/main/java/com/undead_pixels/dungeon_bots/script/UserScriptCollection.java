package com.undead_pixels.dungeon_bots.script;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A collection of UserScripts  
 * TODO:  does this need to be thread-safe?  The GUI only works on one thread, but I know the 
 * scripting system draws from it and it seems like it would need thread safety for that 
 * reason.
 *
 */
public class UserScriptCollection implements Iterable<UserScript>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, UserScript> storage = new HashMap<>();

	public void add(UserScript script) {
		storage.put(script.name, script);
		// TODO - this doesn't handle the editing of names
	}
	
	public UserScript get(String name) {
		return storage.getOrDefault(name, null);
	}
	public void remove(String name) {
		storage.remove(name);
	}

	public void clear() {
		storage.clear();
	}

	@Override
	public Iterator<UserScript> iterator() {
		return storage.values().iterator();
	}
	
	/**Returns an array of all contained UserScripts, sorted by name.*/
	public UserScript[] toArray(){
		UserScript[] result =  storage.values().toArray(new UserScript[storage.values().size()]);
		Arrays.sort(result);
		return result;
	}
}
