/**
 * 
 */
package com.undead_pixels.dungeon_bots.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author kevin
 *
 */
public class WorldEvent<T> {
	
	private HashMap<Object, HashSet<Consumer<T>>> listeners = new HashMap<>();
	
	public synchronized void fire(T arg) {
		for(HashSet<Consumer<T>> h : listeners.values()) {
			for(Consumer<T> l : new ArrayList<>(h)) {
				l.accept(arg);
			}
		}
	}
	
	public synchronized void addListener(Object owner, Consumer<T> listener) {
		HashSet<Consumer<T>> h = listeners.get(owner);
		if(h == null) {
			h = new HashSet<>();
			listeners.put(owner, h);
		}
		
		h.add(listener);
	}
	
	public synchronized HashSet<Consumer<T>> removeListenerFamily(Object owner) {
		return listeners.remove(owner);
	}
	
}
