package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;

public class Key extends Item {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Key(World w, String name, String descr) {
		super(w,name, descr, 0, 0);
	}
}
