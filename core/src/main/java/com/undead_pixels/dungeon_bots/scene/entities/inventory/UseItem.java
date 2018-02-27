package com.undead_pixels.dungeon_bots.scene.entities.inventory;

public interface UseItem {
	default Boolean useItem(ItemReference item) {
		return false;
	}
	default Boolean giveItem(ItemReference item) { return false; }
}
