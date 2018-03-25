package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import org.luaj.vm2.LuaValue;

@Doc("A Key is an Item that is typically used to Open Doors or Item Chests")
public class Key extends Item {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Key(World w, String name, String descr) {
		super(w,name, descr, 0, 0);
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.DEFAULT, doc="Create a new Key item")
	public static Key create(
			@Doc("The World the Key should belong to") LuaValue world) {
		World w = (World)world.checktable().get("this").checkuserdata(World.class);
		return new Key(w, "Key", "A Key that can be used to unlock Doors or Item Chests");
	}
}
