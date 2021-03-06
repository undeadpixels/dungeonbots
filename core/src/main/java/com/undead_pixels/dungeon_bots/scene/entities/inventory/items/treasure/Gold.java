package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

@Doc("Gold is a Treasure that has a value that is a function of its weight.")
public final class Gold extends Treasure {
	private static final long serialVersionUID = 1L;
	public Gold(World w, int weight) {
		super(w, "Gold", 100 * weight, weight);
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Gold item")
	@BindTo("new")
	public static Gold create(
			@Doc("The World the gold belongs to") LuaValue world,
			@Doc("The weight of the Gold item") LuaValue weight) {
		return new Gold(
				(World)world.checktable().get("this").checkuserdata(World.class),
				weight.checkint());
	}
}
