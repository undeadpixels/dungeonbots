package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("A Spell book is a powerful weapon that has a medium range and is quite slow.")
public final class SpellBook extends Weapon {

	public SpellBook(World w) {
		super(w, "Spell Book", 30, 5, new WeaponStats(10,5,2));
	}
}
