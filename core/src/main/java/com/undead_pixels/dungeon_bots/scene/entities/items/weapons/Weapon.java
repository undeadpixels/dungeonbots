package com.undead_pixels.dungeon_bots.scene.entities.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.items.Item;

public abstract class Weapon extends Item {

	final WeaponStats weaponStats;

	public Weapon(World w, String descr, int value, int weight, WeaponStats weaponStats) {
		super(w, "Weapon", descr, value, weight);
		this.weaponStats = weaponStats;
	}
}
