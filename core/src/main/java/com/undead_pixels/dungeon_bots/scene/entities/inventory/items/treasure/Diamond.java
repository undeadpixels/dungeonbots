package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("An Item is a Treasure that is very valuable!")
public final class Diamond extends Treasure {
	public Diamond(World w, String descr, int value, int weight) {
		super(w, "Diamond", 1000, 1);
	}
}
