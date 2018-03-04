package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.entities.Useable;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import java.io.Serializable;

public final class ItemReference implements GetLuaFacade, Serializable, Useable {

	final Inventory inventory;
	final int index;

	public ItemReference(final Inventory inventory, int index) {
		this.inventory = inventory;
		this.index = index;
	}

	/**
	 * Returns the item associated with the ItemReference if it exists.
	 * @return An Optional possibly containing the item.
	 */
	public Item getItem() {
		return inventory.getItem(this);
	}

	/**
	 * Returns true if the ItemReference contains an item.
	 * @return True if the ItemReference contains an item.
	 */
	public boolean hasItem() {
		return getItem().isEmpty();
	}

	/**
	 * Returns a String name of the associated Item
	 * @return The Name of the Item
	 */
	@Override
	@Bind(SecurityLevel.DEFAULT)
	public String getName() {
		return getItem().getName();
	}

	/**
	 * Returns a Description of the Item
	 * @return A Description of the Item
	 */
	@Bind(SecurityLevel.DEFAULT)
	public String getDescription() {
		return getItem().getDescription();
	}

	/**
	 * Returns the Value of the Item.
	 * @return An Integer value of the Item.
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Integer getValue() {
		return getItem().getValue();
	}

	/**
	 * Returns the Weight of the Item.
	 * @return An Integer weight of the Item.
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Integer getWeight() {
		return getItem().getWeight();
	}

	/**
	 * Contextually uses the Item. Action varies depending on the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean use() {
		return getItem().use();
	}

	/**
	 * Uses the Item at the Location up relative to owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean up() {
		return getItem().up();
	}

	/**
	 * Uses the Item at the Location down relative to the owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean down() {
		return getItem().down();
	}

	/**
	 *
	 * @return
	 */
	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean left() {
		return getItem().left();
	}

	/**
	 *
	 * @return
	 */
	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean right() {
		return getItem().right();
	}

	/**
	 *
	 * @return
	 */
	public Item derefItem() {
		Item i = getItem();
		this.inventory.removeItem(this.index);
		return i;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}
}
