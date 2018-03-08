package com.undead_pixels.dungeon_bots.scene.entities.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;

public final class SpellBook extends Weapon {

	public SpellBook(World w) {
		super(w, "Spell Book", 30, 5, new WeaponStats(10,5,2));
	}
}
