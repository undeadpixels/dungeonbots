package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("A Bow is a ranged weapon with a low attack speed.")
public final class Bow extends Weapon {
	public Bow(World w) {
		super(w, "Bow", 30, 5, new WeaponStats(5, 3, 10));
	}
}
