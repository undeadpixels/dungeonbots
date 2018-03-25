package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public interface HasInventory {
	Inventory getInventory();

	@Bind(value = SecurityLevel.DEFAULT, doc = "Peek at the Inventory of the associated entity")
	default LuaValue peekInventory() {
		final Inventory inv = this.getInventory();
		final LuaTable lt = new LuaTable();
		for(int i = 0; i < inv.inventory.length; i++) {
			final ItemReference itemReference = inv.inventory[i];
			final LuaTable pair = new LuaTable();
			pair.set("name", LuaValue.valueOf(itemReference.getName()));
			pair.set("description", LuaValue.valueOf(itemReference.getDescription()));
			lt.set(i + 1, pair);
		}
		return lt;
	}
}
