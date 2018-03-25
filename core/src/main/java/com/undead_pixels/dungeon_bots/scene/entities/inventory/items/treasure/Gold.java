package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

@Doc("Gold is a Treasure that has a value that is a function of it's weight.")
public final class Gold extends Treasure {
	public Gold(World w, int weight) {
		super(w, "Gold", 100 * weight, weight);
	}

	@Bind(value = SecurityLevel.DEFAULT, doc = "Create a new Gold item")
	public static Gold create(
			@Doc("The World the gold belongs to") LuaValue world,
			@Doc("The weight of the Gold item") LuaValue weight) {
		return new Gold(
				(World)world.checktable().get("this").checkuserdata(World.class),
				weight.checkint());
	}
}
