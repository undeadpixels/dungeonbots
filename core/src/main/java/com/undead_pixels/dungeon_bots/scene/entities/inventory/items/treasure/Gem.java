package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("Gems are a Treasure that have some value")
public final class Gem extends Treasure {

	public Gem(World w ) {
		super(w, "Gem", 25, 1);
	}
}
