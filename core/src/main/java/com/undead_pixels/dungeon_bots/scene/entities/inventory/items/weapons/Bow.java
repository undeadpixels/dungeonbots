package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;

public final class Bow extends Weapon {
	public Bow(World w) {
		super(w, "Bow", 30, 5, new WeaponStats(5, 3, 10));
	}
}
