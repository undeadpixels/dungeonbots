package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;

import java.io.Serializable;
import java.util.Optional;

public class ItemReference implements GetLuaFacade, Serializable {

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

	@Bind(SecurityLevel.DEFAULT)
	public String getName() {
		return Optional.ofNullable(inventory.inventory[index]).map(item -> item.getName()).orElse("Empty");
	}

	@Bind(SecurityLevel.DEFAULT)
	public String getDescription() {
		return Optional.ofNullable(inventory.inventory[index]).map(item -> item.getDescription()).orElse("An empty item slot!");
	}

	@Bind(SecurityLevel.DEFAULT)
	public Integer getValue() {
		return Optional.ofNullable(inventory.inventory[index]).map(item -> item.getValue()).orElse(0);
	}

	@Bind(SecurityLevel.DEFAULT)
	public Integer getWeight() {
		return Optional.ofNullable(inventory.inventory[index]).map(item -> item.getWeight()).orElse(0);
	}

	@Bind(SecurityLevel.DEFAULT)
	public Boolean use() {
		return Optional.ofNullable(inventory.inventory[index]).map(item -> item.use()).orElse(false);
	}

	public Optional<Item> derefItem() {
		Optional<Item> i = getItem();
		this.inventory.removeItem(this.index);
		return i;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}
}
