/**
 * 
 */
package com.undead_pixels.dungeon_bots.scene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * @author kevin
 *
 */
public class WorldEvent<T> {
	
	private HashMap<Object, HashSet<Consumer<T>>> listeners = new HashMap<>();
	
	public void fire(T arg) {
		for(HashSet<Consumer<T>> h : listeners.values()) {
			for(Consumer<T> l : h) {
				l.accept(arg);
			}
		}
	}
	
	public void addListener(Object owner, Consumer<T> listener) {
		HashSet<Consumer<T>> h = listeners.get(owner);
		if(h == null) {
			h = new HashSet<>();
			listeners.put(owner, h);
		}
		
		h.add(listener);
	}
	
	public HashSet<Consumer<T>> removeListenerFamily(Object owner) {
		return listeners.remove(owner);
	}
	
}
