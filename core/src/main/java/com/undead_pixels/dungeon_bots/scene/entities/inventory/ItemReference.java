package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.entities.Useable;
import com.undead_pixels.dungeon_bots.scene.entities.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import java.io.Serializable;

public final class ItemReference implements GetLuaFacade, Serializable, Useable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * All ItemReferences are associated with a corresponding index into an Inventory.
	 */
	public final int index;

	/**
	 * The Underlying item referenced by the ItemReference.
	 */
	Item item;

	public ItemReference(int index) {
		this.index = index;
		item = new Item.EmptyItem();
	}

	/**
	 * Returns the item associated with the ItemReference if it exists.
	 * @return An Optional possibly containing the item.
	 */
	public Item getItem() {
		return item;
	}

	public ItemReference setItem(Item i) {
		this.item = i;
		return this;
	}

	/**
	 * Returns true if the ItemReference contains an item.
	 * @return True if the ItemReference contains an item.
	 */
	public boolean hasItem() {
		return item.isEmpty();
	}

	/**
	 * Returns a String name of the associated Item
	 * @return The Name of the Item
	 */
	@Override
	@Bind(SecurityLevel.DEFAULT) @BindTo("name")
	public String getName() {
		return item.getName();
	}

	/**
	 * Returns a Description of the Item
	 * @return A Description of the Item
	 */
	@Bind(SecurityLevel.DEFAULT) @BindTo("description")
	public String getDescription() {
		return item.getDescription();
	}

	/**
	 * Returns the Value of the Item.
	 * @return An Integer value of the Item.
	 */
	@Bind(SecurityLevel.DEFAULT) @BindTo("value")
	public Integer getValue() {
		return item.getValue();
	}

	/**
	 * Returns the Weight of the Item.
	 * @return An Integer weight of the Item.
	 */
	@Bind(SecurityLevel.DEFAULT) @BindTo("weight")
	public Integer getWeight() {
		return item.getWeight();
	}

	/**
	 * Contextually uses the Item. Action varies depending on the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean use() {
		return item.use();
	}

	/**
	 * Uses the Item at the Location up relative to owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean up() {
		return item.up();
	}

	/**
	 * Uses the Item at the Location down relative to the owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean down() {
		return item.down();
	}

	/**
	 *
	 * @return
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean left() {
		return item.left();
	}

	/**
	 *
	 * @return
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean right() {
		return item.right();
	}

	/**
	 *
	 * @return
	 */
	public Item derefItem() {
		Item i = item;
		this.item = new Item.EmptyItem();
		return i;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}
}
