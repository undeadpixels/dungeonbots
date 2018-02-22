package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

import java.util.Optional;

public class ItemReference {

	final Inventory inventory;
	final int index;

	public ItemReference(Inventory inventory, int index) {
		this.inventory = inventory;
		this.index = index;
	}

	public Optional<Item> getItem() {
		return Optional.ofNullable(inventory.inventory[this.index]);
	}

	public boolean hasItem() {
		return inventory.inventory[index] != null;
	}

	public String getName() {
		return Optional.ofNullable(inventory.inventory[index].getName()).orElse("Empty");
	}

	public String getDescription() {
		return Optional.ofNullable(inventory.inventory[index].getDescription()).orElse("An empty item slot!");
	}

	public Integer getValue() {
		return Optional.ofNullable(inventory.inventory[index].getValue()).orElse(0);
	}

	public Integer getWeight() {
		return Optional.ofNullable(inventory.inventory[index].getWeight()).orElse(0);
	}

	public Optional<Item> derefItem() {
		Optional<Item> i = getItem();
		this.inventory.removeItem(this.index);
		return i;
	}
}
