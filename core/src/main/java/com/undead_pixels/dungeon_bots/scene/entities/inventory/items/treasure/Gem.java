package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

@Doc("Gems are a Treasure that have some value")
public final class Gem extends Treasure {
	private static final long serialVersionUID = 1L;
	public Gem(World w) {
		super(w, "Gem", 25, 1);
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Creates a new Gem")
	@BindTo("new")
	public static Gem create(
			@Doc("The World the Gem belongs to") LuaValue world) {
		return new Gem((World)world.checktable().get("this").checkuserdata(World.class));
	}
}
