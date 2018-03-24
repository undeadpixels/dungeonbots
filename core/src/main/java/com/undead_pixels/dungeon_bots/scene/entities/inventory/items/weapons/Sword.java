package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

@Doc("A Sword is a fast weapon that does a reasonable amount of damage, but has a limited range.")
public final class Sword extends Weapon {
	public Sword(World w) {
		super(w, "Sword", 30, 10, new WeaponStats(7,10,1));
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.DEFAULT, doc = "Create a new Sword item")
	public static Sword create(
			@Doc("The World the Sword should belong to") LuaValue world) {
		return new Sword((World)world.checktable().get("this").checkuserdata(World.class));
	}
}
