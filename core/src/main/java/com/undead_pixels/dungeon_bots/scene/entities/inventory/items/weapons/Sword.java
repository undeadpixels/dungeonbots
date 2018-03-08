package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;

public final class Sword extends Weapon {
	public Sword(World w) {
		super(w, "Sword", 30, 10, new WeaponStats(7,10,1));
	}
}
