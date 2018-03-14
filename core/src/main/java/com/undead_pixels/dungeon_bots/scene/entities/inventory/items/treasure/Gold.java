package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("Gold is a Treasure that has a value that is a function of it's weight.")
public final class Gold extends Treasure {
	public Gold(World w, int weight) {
		super(w, "Gold", 100 * weight, weight);
	}
}
