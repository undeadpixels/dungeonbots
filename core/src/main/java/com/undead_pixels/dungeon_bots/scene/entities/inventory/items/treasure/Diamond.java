package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

@Doc("An Item is a Treasure that is very valuable!")
public final class Diamond extends Treasure {
	public Diamond(World w) {
		super(w, "Diamond", 1000, 1);
	}

	@Bind(value = SecurityLevel.DEFAULT, doc = "Create a Diamond")
	@BindTo("new")
	public static Diamond create(@Doc("The World the Diamond belongs to") LuaValue world) {
		return new Diamond((World)world.checktable().get("this").checkuserdata(World.class));
	}
}
