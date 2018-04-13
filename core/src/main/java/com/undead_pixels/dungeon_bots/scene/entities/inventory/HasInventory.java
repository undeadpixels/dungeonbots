package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.ItemInfo;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public interface HasInventory {
	Inventory getInventory();

	@Bind(value = SecurityLevel.DEFAULT, doc = "Peek at the Inventory of the associated entity\n" +
			"Returns a list of tables with a 'name' and 'description' keys with the associated values.")
	default LuaValue peekInventory() {
		final Inventory inv = this.getInventory();
		final LuaTable lt = new LuaTable();
		for(int i = 0, index = 0; index < inv.inventory.length; index++) {
			final ItemReference ir = inv.inventory[index];
			if(ir.hasItem()) {
				lt.set(i + 1, new ItemInfo(ir.getName(), ir.getDescription(), index + 1, ir.getWeight(), ir.getValue()).getLuaValue());
				i++;
			}
		}
		return lt;
	}

	default Boolean canTake() {
		return false;
	}
}
