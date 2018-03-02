package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;

import java.io.Serializable;
import java.util.Optional;

public final class ItemReference implements GetLuaFacade, Serializable, Useable {

	final Inventory inventory;
	private final int index;

	public ItemReference(final Inventory inventory, int index) {
		this.inventory = inventory;
		this.index = index;
	}

	public Optional<Item> getItem() {
		return Optional.ofNullable(inventory.inventory[this.index]);
	}

	public boolean hasItem() {
		return inventory.inventory[index] != null;
	}

	@Override
	@Bind(SecurityLevel.DEFAULT)
	public String getName() {
		return getItem()
				.map(Item::getName)
				.orElse("Empty");
	}

	@Bind(SecurityLevel.DEFAULT)
	public String getDescription() {
		return getItem()
				.map(Item::getDescription)
				.orElse("An empty item slot");
	}

	@Bind(SecurityLevel.DEFAULT)
	public Integer getValue() {
		return getItem()
				.map(Item::getValue)
				.orElse(0);
	}

	@Bind(SecurityLevel.DEFAULT)
	public Integer getWeight() {
		return getItem()
				.map(Item::getWeight)
				.orElse(0);
	}


	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean use() {
		return getItem()
				.map(Useable::use)
				.orElse(false);
	}

	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean up() {
		return getItem()
				.map(Useable::up)
				.orElse(false);
	}

	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean down() {
		return getItem()
				.map(Useable::down)
				.orElse(false);
	}

	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean left() {
		return getItem()
				.map(Useable::left)
				.orElse(false);
	}

	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean right() {
		return getItem()
				.map(Useable::right)
				.orElse(false);
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
