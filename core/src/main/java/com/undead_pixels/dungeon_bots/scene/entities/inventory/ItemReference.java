package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Useable;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.LuaValue;

import java.awt.geom.Point2D;
import java.io.Serializable;

@Doc("Inventory Item type that represents an item at a given spot in an Inventory.\n" +
		"Item changes if the Inventory is modified.")
public final class ItemReference implements GetLuaFacade, Serializable, Useable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * All ItemReferences are associated with a corresponding index into an Inventory.
	 */
	public final int index;

	Inventory inventory;

	/**
	 * The Underlying item referenced by the ItemReference.
	 */
	Item item;

	public ItemReference(Inventory inventory, int index) {
		this.index = index;
		this.inventory = inventory;
		item = new Item.EmptyItem();
	}

	/**
	 * Returns the item associated with the ItemReference if it exists.
	 * @return An Optional possibly containing the item.
	 */
	@Doc("Gets the referenced Item")
	@Bind(SecurityLevel.AUTHOR)
	public Item getItem() {
		return item;
	}

	@Bind(SecurityLevel.AUTHOR)
	@Doc("Sets the underlying value of the Item Reference to the provided Item.")
	public ItemReference setItem(@Doc("An Item") LuaValue i) {
		return setItem((Item)i.checktable().get("this").checkuserdata(Item.class));
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
	@Doc("Asks the Item Reference for the name of the given Item")
	public String getName() {
		return item.getName();
	}

	/**
	 * Returns a Description of the Item
	 * @return A Description of the Item
	 */
	@Bind(SecurityLevel.DEFAULT) @BindTo("description")
	@Doc("Asks the Item Reference for a description of the given Item")
	public String getDescription() {
		return item.getDescription();
	}

	/**
	 * Returns the Value of the Item.
	 * @return An Integer value of the Item.
	 */
	@Bind(SecurityLevel.DEFAULT) @BindTo("value")
	@Doc("Asks the Item Reference for the monetary value of the given Item")
	public Integer getValue() {
		return item.getValue();
	}

	/**
	 * Returns the Weight of the Item.
	 * @return An Integer weight of the Item.
	 */
	@Bind(SecurityLevel.DEFAULT) @BindTo("weight")
	@Doc("Asks the Item Reference for the weight of the given Item")
	public Integer getWeight() {
		return item.getWeight();
	}

	/**
	 * Contextually uses the Item. Action varies depending on the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	@Doc("Contextually uses the Item. Action varies depending on the Item.")
	public Boolean use() {
		return inventory.owner.getWorld()
				.tryUse(this, inventory.owner.getPosition());
	}

	/**
	 * Uses the Item at the Location up relative to owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	@Doc("Uses the Item at the Location up relative to owner of the Item")
	public Boolean up() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D newPos = new Point2D.Float(pos.x, pos.y - 1.0f);
		return owner.getWorld().tryUse(this, newPos);
	}

	/**
	 * Uses the Item at the Location down relative to the owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Doc("Uses the Item at the Location down relative to the owner of the Item")
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean down() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D newPos = new Point2D.Float(pos.x, pos.y + 1.0f);
		return owner.getWorld().tryUse(this, newPos);
	}

	/**
	 * Uses the Item at the Location left relative to the owner of the Item.
	 * @return
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	@Doc("Uses the Item at the Location left relative to the owner of the Item")
	public Boolean left() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D newPos = new Point2D.Float(pos.x - 1.0f, pos.y);
		return owner.getWorld().tryUse(this, newPos);
	}

	/**
	 * Uses the Item at the Location right relative to the owner of the Item
	 * @return
	 */
	@Doc("Uses the Item at the Location right relative to the owner of the Item")
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean right() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D newPos = new Point2D.Float(pos.x + 1.0f, pos.y);
		return owner.getWorld().tryUse(this, newPos);
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
