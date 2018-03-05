package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;

public class Question extends Item {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Question(World w, String name, String descr, int value, int weight) {
		super(w, name, descr, value, weight);
	}
}
