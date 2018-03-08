package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;

public abstract class Weapon extends Item {

	final WeaponStats weaponStats;

	Weapon(World w, String descr, int value, int weight, WeaponStats weaponStats) {
		super(w, "Weapon", descr, value, weight);
		this.weaponStats = weaponStats;
	}

	public WeaponStats getWeaponStats() {
		return weaponStats;
	}

	@Override
	public String getDescription() {
		return String.format(
				"Weapon: %s\n\tDamage: %d\n\tAttack Speed: %d\n\tRange: %d",
				description,
				weaponStats.damage,
				weaponStats.speed,
				weaponStats.range);
	}
}
