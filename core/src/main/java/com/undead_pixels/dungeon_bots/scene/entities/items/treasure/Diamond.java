package com.undead_pixels.dungeon_bots.scene.entities.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;

public final class Diamond extends Treasure {
	public Diamond(World w, String descr, int value, int weight) {
		super(w, "Diamond", 1000, 1);
	}
}
