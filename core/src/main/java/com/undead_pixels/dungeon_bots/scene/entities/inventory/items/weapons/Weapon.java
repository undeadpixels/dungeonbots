package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

@Doc("A Weapon is an Item that can be used to damage adjacent Entities not on the players Team.\n" +
		"Different types of weapons have different properties, such as more Damage, more Range, or\n" +
		"a higher attack Speed (less cool down after an attack)")
public abstract class Weapon extends Item {

	final WeaponStats weaponStats;

	Weapon(World w, String descr, int value, int weight, WeaponStats weaponStats) {
		super(w, "Weapon", descr, value, weight);
		this.weaponStats = weaponStats;
	}

	@Doc("Gets the Stats of the Weapon")
	@Bind(SecurityLevel.AUTHOR) @BindTo("stats")
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
