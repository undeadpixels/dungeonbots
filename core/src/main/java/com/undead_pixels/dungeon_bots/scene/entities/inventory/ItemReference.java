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

	/**
	 * Returns the item associated with the ItemReference if it exists.
	 * @return An Optional possibly containing the item.
	 */
	public Optional<Item> getItem() {
		return Optional.ofNullable(inventory.inventory[this.index]);
	}

	/**
	 * Returns true if the ItemReference contains an item.
	 * @return True if the ItemReference contains an item.
	 */
	public boolean hasItem() {
		return getItem().isPresent();
	}

	/**
	 * Returns a String name of the associated Item
	 * @return The Name of the Item
	 */
	@Override
	@Bind(SecurityLevel.DEFAULT)
	public String getName() {
		return getItem()
				.map(Item::getName)
				.orElse("Empty");
	}

	/**
	 * Returns a Description of the Item
	 * @return A Description of the Item
	 */
	@Bind(SecurityLevel.DEFAULT)
	public String getDescription() {
		return getItem()
				.map(Item::getDescription)
				.orElse("An empty item slot");
	}

	/**
	 * Returns the Value of the Item.
	 * @return An Integer value of the Item.
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Integer getValue() {
		return getItem()
				.map(Item::getValue)
				.orElse(0);
	}

	/**
	 * Returns the Weight of the Item.
	 * @return An Integer weight of the Item.
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Integer getWeight() {
		return getItem()
				.map(Item::getWeight)
				.orElse(0);
	}

	/**
	 * Contextually uses the Item. Action varies depending on the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean use() {
		return getItem()
				.map(Useable::use)
				.orElse(false);
	}

	/**
	 * Uses the Item at the Location up relative to owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean up() {
		return getItem()
				.map(Useable::up)
				.orElse(false);
	}

	/**
	 * Uses the Item at the Location down relative to the owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean down() {
		return getItem()
				.map(Useable::down)
				.orElse(false);
	}

	/**
	 *
	 * @return
	 */
	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean left() {
		return getItem()
				.map(Useable::left)
				.orElse(false);
	}

	/**
	 *
	 * @return
	 */
	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean right() {
		return getItem()
				.map(Useable::right)
				.orElse(false);
	}

	/**
	 *
	 * @return
	 */
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
