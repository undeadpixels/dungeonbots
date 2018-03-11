package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;

public interface ApplyItem {
	/**
	 * Applies the item to the given entity.
	 * Boolean flag return determines if Item should be removed from inventory
	 * after being applied.
 	 * @param e The Entity the Item is being applied to.
	 * @return Whether the Item should be removed from the Inventory.
	 */
	default Boolean applyTo(Entity e) {
		return true;
	}
}
