package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("Treasures are Items that are have high monetary value.")
public abstract class Treasure extends Item {
	public Treasure(World w, String descr, int value, int weight) {
		super(w, "Treasure", descr, value, weight);
	}

}
