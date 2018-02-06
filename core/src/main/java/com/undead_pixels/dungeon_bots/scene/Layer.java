package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;

import java.util.ArrayList;

/**
 * A class to represent a collection of actors at a given Z-value
 * Used to draw some things on top of other things.
 */
public class Layer implements Comparable<Layer> {
	/**
	 * The z value
	 */
	private final float z;

	/**
	 * Constructor
	 * @param z
	 */
	public Layer(float z) {
		super();
		this.z = z;
	}

	/**
	 * Internal storage
	 */
	private ArrayList<Entity> entities = new ArrayList<Entity>();

	@Override
	public int compareTo(Layer o) {
		if(z == o.z) {
			return 0;
		} else if(z < o.z) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * @param e	The entity to add
	 */
	public void add(Entity e) {
		entities.add(e);
	}

	/**
	 * @return	A list of all entities in this layer
	 */
	public ArrayList<Entity> getEntities() {
		return entities;
	}

}