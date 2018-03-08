package com.undead_pixels.dungeon_bots.scene.entities.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;

public final class Gold extends Treasure {
	public Gold(World w, int weight) {
		super(w, "Gold", 100 * weight, weight);
	}
}
