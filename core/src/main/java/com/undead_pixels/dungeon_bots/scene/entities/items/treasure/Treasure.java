package com.undead_pixels.dungeon_bots.scene.entities.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.items.Item;

public abstract class Treasure extends Item {
	public Treasure(World w, String descr, int value, int weight) {
		super(w, "Treasure", descr, value, weight);
	}

	@Override
	public Boolean use() {
		/* Produce a sound and render a glint of light */
		return true;
	}
}
