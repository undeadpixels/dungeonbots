package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

@Doc("A Bow is a ranged weapon with a low attack speed.")
public final class Bow extends Weapon {
	private static final long serialVersionUID = 1L;
	public Bow(World w) {
		super(w, "Bow", 30, 5, new WeaponStats(5, 3, 10));
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.DEFAULT, doc="Create a new Bow item")
	public static Bow create(
			@Doc("The World that the Bow should belong to") LuaValue world) {
		return new Bow((World)world.checktable().get("this").checkuserdata(World.class));
	}
}
