package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("A Sword is a fast weapon that does a reasonable amount of damage, but has a limited range.")
public final class Sword extends Weapon {
	public Sword(World w) {
		super(w, "Sword", 30, 10, new WeaponStats(7,10,1));
	}
}
