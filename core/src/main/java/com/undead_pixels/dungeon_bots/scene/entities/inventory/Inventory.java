package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.HasImage;
import com.undead_pixels.dungeon_bots.scene.entities.ItemChest;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.*;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.*;

/**
 * A type that encapsulates handling accessing and modifying
 * an inventory for an entity.
 */
@Doc("An Inventory is a data type that has functionality supporting accessing and retrieving Item Types")
public class Inventory implements GetLuaFacade, Serializable, HasImage {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final ItemReference[] inventory;

	/**
	 * The max size of the inventory.
	 */
	private final int maxSize;

	/**
	 * The max weight of the inventory.
	 */
	private final int maxWeight = 1000;

	final Entity owner;

	public Inventory(Entity owner, int maxSize) {
		this.maxSize = maxSize;
		inventory = new ItemReference[maxSize];
		this.owner = owner;
		IntStream.range(0, inventory.length).forEach(i -> inventory[i] = new ItemReference(this, i));
		owner.getDefaultWhitelist().addAutoLevelsForBindables(ItemReference.class);
	}

	/**
	 * Lua Binding to return but not remove an item at the associated index
	 * from the inventory.
	 * @param index The index in the inventory to return an item.
	 * @return The Item
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Get the ItemReference at the specified index of the Inventory")
	public ItemReference peek(@Doc("The Index into the Inventory") LuaValue index) {
		final int i = index.checkint() - 1;
		assert i < this.inventory.length;
		return inventory[i];
	}

	public ItemReference peek(int index) {
		assert index < this.inventory.length;
		return inventory[index];
	}

	/**
	 *
	 * @param i
	 * @return
	 */
	public Item removeItem(int i) {
		assert i < this.inventory.length;
		synchronized (this.inventory) {
			Item item = this.inventory[i].getItem();
			this.inventory[i].setItem(new Item.EmptyItem());
			return item;
		}
	}

	/**
	 * Find the Index of an item in the Inventory if it exists
	 * @param item The Item possibly contained in the inventory
	 * @return An Optional of the Index of the Item into the Inventory.
	 */
	public Optional<Integer> findIndex(final Item item) {
		return Stream.of(inventory).filter(itemReference -> itemReference.getItem() == item)
				.findFirst()
				.map(val -> val.index);
	}

	@Bind(value = SecurityLevel.ENTITY,
			doc = "Get the underlying Item contained by the ItemReference at the index in the Inventory")
	public Item getItem(@Doc("The Index of the Item") LuaValue index) {
		return this.inventory[index.checkint()].getItem();
	}

	/**
	 *
	 * @return
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Query the total weight of items in the inventory")
	public Integer weight() {
		return Stream.of(inventory)
				.reduce(0, (num, item) -> num + item.getWeight(), (a, b) -> a + b);
	}

	/**
	 * Calculate the current capacity of the Inventory by calculating the number of non-empty<br>
	 * items held within.
	 * @return An integer representing the number of items currently held in the inventory
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Query the current capacity of the Inventory")
	public Integer capacity() {
		return Stream.of(inventory)
				.reduce(0, (num, item) -> num + (item.getItem().isEmpty() ? 0 : 1), (a,b) -> a + b);
	}

	/**
	 *
	 * @param item
	 * @return
	 */
	public boolean addItem(Item item) {
		if(item.isEmpty())
			return true;
		synchronized (this.inventory) {
			if(weight() + item.getWeight() > maxWeight)
				return false;
			for(int i = 0; i < this.inventory.length; i++) {
				if(inventory[i].getItem().isEmpty()) {
					inventory[i].setItem(item);
					this.owner.getSandbox().fireEvent("INV_MODIFIED");
					this.owner.getSandbox().fireEvent("ITEM_GIVEN");
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Overload of method that only derefs item if an item can be 'added'
	 * Otherwise, does not deref the item from the ItemReference
	 * @param ir
	 * @return
	 */
	public Integer tryTakeItem(final ItemReference ir) {
		if(ir.item.isEmpty())
			return -1;
		synchronized (this.inventory) {
			if(weight() + ir.item.getWeight() > maxWeight)
				return -1;
			for(int i = 0; i < this.inventory.length; i++) {
				if(inventory[i].getItem().isEmpty()) {
					inventory[i].setItem(ir.derefItem());
					owner.getSandbox().fireEvent("ITEM_GIVEN", inventory[i].getLuaValue());
					owner.getSandbox().fireEvent("INV_MODIFIED");
					return i;
				}
			}
			return -1;
		}
	}

	/**
	 * Overload of method that only derefs item if an item can be 'added'
	 * Otherwise, does not deref the item from the ItemReference
	 * @param ir
	 * @return
	 */
	public Integer tryTakeItem(final ItemReference ir, int index) {
		if(ir.item.isEmpty())
			return -1;
		synchronized (this.inventory) {
			if(weight() + ir.item.getWeight() > maxWeight)
				return -1;
			if(inventory[index].getItem().isEmpty()) {
				inventory[index].setItem(ir.derefItem());
				owner.getSandbox().fireEvent("ITEM_GIVEN", inventory[index].getLuaValue());
				owner.getSandbox().fireEvent("INV_MODIFIED");
				return index + 1;
			}
			return -1;
		}
	}

	@Bind(value = SecurityLevel.ENTITY,
			doc = "Adds the Item to the Inventory if possible")
	public Boolean addItem(@Doc("The Item to add") LuaValue item) {
		return addItem((Item)item.checktable()
				.get("this")
				.checkuserdata(Item.class));
	}

	public boolean containsItem(Item i) {
		return getItems().contains(i);
	}

	/**
	 *
	 * @param luaItem An ItemReference to an item in an inventory.
	 * @return If the item was added to the inventory
	 */
	@Bind(value = SecurityLevel.ENTITY,
			doc = "Puts an ItemReferences Item into this inventory")
	public Integer putItem(@Doc("The ItemReference to deref and place into this inventory") LuaValue luaItem) {
		return tryTakeItem(ItemReference.class.cast(luaItem.checktable().get("this").checkuserdata(ItemReference.class)));
	}

	/**
	 *
	 * @return
	 */
	@Bind(value=SecurityLevel.DEFAULT,doc = "Get the Size of the Inventory")
	public Integer size() {
		return this.maxSize;
	}

	@Bind(value=SecurityLevel.DEFAULT,doc = "Get the Max Weight this Inventory can support")
	public Integer maxWeight() {
		return maxWeight;
	}

	/**
	 *
	 * @return
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Get an array of ItemReferences to the Inventory")
	public LuaTable get() {
		final LuaTable table = new LuaTable();
		for(int i = 0; i < inventory.length; i++) {
			table.set(i + 1, inventory[i].getLuaValue());
		}
		return table;
	}

	/**
	 * Binding that unpacks the contents of an inventory in to Varargs.<br>
	 * Example of use<br>
	 * <pre>{@code
	 * 	i1, i2 = player:inventory():unpack()
	 * }</pre>
	 * @return
	 */
	@Bind(value = SecurityLevel.DEFAULT,
			doc = "Unpacks the Inventory contents to a Lua Varargs type")
	public Varargs unpack() {
		final LuaValue[] ans = new LuaValue[inventory.length];
		IntStream.range(0, inventory.length)
				.forEach(i -> ans[i] = inventory[i].getLuaValue());
		return LuaValue.varargsOf(ans);
	}

	/**
	 *
	 */
	@Bind(value = SecurityLevel.AUTHOR,
			doc = "Resets the inventory to Empty Item values")
	public void reset() {
		Stream.of(inventory).forEach(itemRef ->
				itemRef.setItem(new Item.EmptyItem()));
		this.owner.getSandbox().fireEvent("INV_MODIFIED");
	}

	/**
	 *
	 * @return
	 */
	public List<Item> getItems() {
		return Stream.of(inventory)
				.map(ItemReference::getItem)
				.collect(Collectors.toList());
	}

	@Override
	@Bind(value = SecurityLevel.NONE,
			doc = "Get a String representation of the Inventory")
	public String toString() {
		final StringBuilder ans = new StringBuilder();
		ans.append("Index\tName\tDescription\tValue\tWeight\n");
		for(final ItemReference ir : inventory) {
			if(ir.hasItem()) {
				ans.append(String.format(
						"%d\t%s\t%s\t%s\t%s\n",
						ir.index + 1,
						ir.getName(),
						ir.getDescription(),
						ir.getValue(),
						ir.getWeight()));
			}
		}
		return ans.toString();
	}

	@BindTo("totalValue")
	@Bind(value = SecurityLevel.NONE,
			doc = "Calculates the total value of the contents of the Inventory")
	public Integer getTotalValue() {
		return Stream.of(inventory)
				.map(i -> i.getValue())
				.reduce(0, (a,b) -> a + b);
	}

	@Bind(value = SecurityLevel.NONE, doc = "Swap the items at the two positions in the inventory")
	public Boolean swap(
			@Doc("The first item to swap") LuaValue first,
			@Doc("The second item to swap") LuaValue second) {
		final int f = first.checkint() - 1;
		final int s = second.checkint() - 1;
		if ((f < this.inventory.length && f > 0) && (s < this.inventory.length && s > 0)) {
			final ItemReference a = inventory[f];
			final ItemReference b = inventory[s];
			final Item swap = a.item;
			a.item = b.item;
			b.item = swap;
			this.owner.getSandbox().fireEvent("INV_MODIFIED");
			return true;
		}
		else
			return false;
	}

	public void swapInventory(final Inventory other) {
		int min = Math.min(this.inventory.length, other.inventory.length);
		for(int i = 0; i < min; i++) {
			final ItemReference left = this.inventory[i];
			final ItemReference right = other.inventory[i];
			final Item swap = left.item;
			left.item = right.item;
			right.item = swap;
		}
		this.owner.getSandbox().fireEvent("INV_MODIFIED");
		other.owner.getSandbox().fireEvent("INV_MODIFIED");
	}

	@Override
	public Image getImage() {
		return ItemChest.LOCKED_TEXTURE.toImage();
	}

	public Entity getOwner() {
		return owner;
	}
}
