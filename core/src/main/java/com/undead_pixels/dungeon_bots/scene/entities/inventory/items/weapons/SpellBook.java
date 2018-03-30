package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

@Doc("A Spell book is a powerful weapon that has a medium range and is quite slow.")
public final class SpellBook extends Weapon {
	private static final long serialVersionUID = 1L;
	public SpellBook(World w) {
		super(w, "Spell Book", 30, 5, new WeaponStats(10,5,2));
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.DEFAULT, doc = "Create a new Spell Book item")
	public static SpellBook create(
			@Doc("The World that the Spell Book should belong to") LuaValue world) {
		return new SpellBook((World)world.checktable().get("this").checkuserdata(World.class));
	}
}
