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
import org.luaj.vm2.Varargs;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Optional;

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

	public final Inventory inventory;

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
	@Override
	@Bind(value=SecurityLevel.DEFAULT, doc="Contextually uses the Item. Action varies depending on the Item.")
	public Boolean use(final Varargs args) {
		if(args.narg() > 0 && args.arg(1).isstring() || args.arg(2).isstring()) {
			final String dir = args.arg1().isstring() ? args.arg1().tojstring() : args.arg(2).tojstring();
			switch (dir.toLowerCase()){
				case "up":
					return useUp();
				case "down":
					return useDown();
				case "left":
					return useLeft();
				case "right":
					return useRight();
				default:
					return false;
			}
		}
		else {
			return useSelf();
		}
	}

	@Override
	@Bind(value=SecurityLevel.DEFAULT, doc="Contextually uses the Item on the caller. Action varies depending on the Item.")
	public Boolean useSelf() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		return owner.getWorld().tryUse(this, pos);
	}

	/**
	 * Uses the Item at the Location up relative to owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override
	@Bind(value = SecurityLevel.DEFAULT, doc = "Uses the Item at the Location up relative to owner of the Item")
	public Boolean useUp() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D.Float newPos = new Point2D.Float(pos.x, pos.y - 1.0f);
		return owner.getWorld().tryUse(this, newPos);
	}

	/**
	 * Uses the Item at the Location down relative to the owner of the Item.
	 * @return True if the Item was successfully used.
	 */
	@Override
	@Bind(value=SecurityLevel.DEFAULT, doc="Uses the Item at the Location down relative to the owner of the Item")
	public Boolean useDown() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D.Float newPos = new Point2D.Float(pos.x, pos.y + 1.0f);
		return owner.getWorld().tryUse(this, newPos);
	}

	/**
	 * Uses the Item at the Location left relative to the owner of the Item.
	 * @return
	 */
	@Override
	@Bind(value=SecurityLevel.DEFAULT, doc="Uses the Item at the Location left relative to the owner of the Item")
	public Boolean useLeft() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D.Float newPos = new Point2D.Float(pos.x - 1.0f, pos.y);
		return owner.getWorld().tryUse(this, newPos);
	}

	/**
	 * Uses the Item at the Location right relative to the owner of the Item
	 * @return
	 */
	@Override
	@Bind(value=SecurityLevel.DEFAULT, doc="Uses the Item at the Location right relative to the owner of the Item")
	public Boolean useRight() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D.Float newPos = new Point2D.Float(pos.x + 1.0f, pos.y);
		return owner.getWorld().tryUse(this, newPos);
	}

	@Override
	@Bind(value=SecurityLevel.DEFAULT,
			doc = "Give the item to any entities found at the specified direction relative to the owner of the item.")
	public Boolean give(@Doc("A direction relative to the entity") final LuaValue dir) {
		switch (dir.tojstring()) {
			case "up":
				return giveUp();
			case "down":
				return giveDown();
			case "left":
				return giveLeft();
			case "right":
				return giveRight();
			default:
				return false;
		}
	}

	@Override
	@Bind(value=SecurityLevel.DEFAULT,
			doc="Give the Item to any entities 'Up' relative to the owner of the item")
	public Boolean giveUp() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D.Float newPos = new Point2D.Float(pos.x, pos.y - 1.0f);
		return owner.getWorld().tryGive(this, newPos);
	}

	@Override
	@Bind(value=SecurityLevel.DEFAULT,
			doc = "Give the Item to any entities 'Down' relative to the owner of the item")
	public Boolean giveDown() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D.Float newPos = new Point2D.Float(pos.x, pos.y + 1.0f);
		return owner.getWorld().tryGive(this, newPos);
	}

	@Override
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Give the Item to any entities 'Left' relative to the owner of the item")
	public Boolean giveLeft() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D.Float newPos = new Point2D.Float(pos.x - 1.0f, pos.y);
		return owner.getWorld().tryGive(this, newPos);
	}

	@Override
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Give the Item to any entities 'Right' relative to the owner of the item")
	public Boolean giveRight() {
		final Entity owner = inventory.owner;
		final Point2D.Float pos = owner.getPosition();
		final Point2D.Float newPos = new Point2D.Float(pos.x + 1.0f, pos.y);
		return owner.getWorld().tryGive(this, newPos);
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
