package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.*;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.*;

/**
 * A type that encapsulates handling accessing and modifying
 * an inventory for an entity.
 */
public class Inventory implements GetLuaFacade, Serializable {
	
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
	private final int maxWeight = 100;

	final Entity owner;

	public Inventory(final Entity owner, int maxSize) {
		this.maxSize = maxSize;
		inventory = new ItemReference[maxSize];
		this.owner = owner;
		IntStream.range(0, maxSize).forEach(i -> inventory[i] = new ItemReference(this, i));
		owner.getDefaultWhitelist().addAutoLevelsForBindables(ItemReference.class);
	}

	@Override
	public int getId() {
		return this.hashCode();
	}

	@Override
	public String getName() {
		return "inventory";
	}

	/**
	 * Lua Binding to return but not remove an item at the associated index
	 * from the inventory.
	 * @param index The index in the inventory to return an item.
	 * @return The Item
	 */
	@Bind(SecurityLevel.DEFAULT) public ItemReference peek(LuaValue index) {
		final int i = index.checkint() - 1;
		assert i < this.inventory.length;
		return inventory[i];
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

	public Optional<Integer> findIndex(Item item) {
		return Stream.of(inventory).filter(itemReference -> itemReference.getItem() == item)
				.findFirst()
				.map(val -> val.index);
	}

	public Item getItem(int index) {
		return this.inventory[index].getItem();
	}

	/**
	 *
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Integer weight() {
		return Stream.of(inventory)
				.reduce(0, (num, item) -> num + item.getWeight(), (a, b) -> a + b);
	}

	/**
	 * Calculate the current capacity of the Inventory by calculating the number of non-empty<br>
	 * items held within.
	 * @return An integer representing the number of items currently held in the inventory
	 */
	@Bind(SecurityLevel.DEFAULT)
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
		synchronized (this.inventory) {
			if(weight() + item.getWeight() > maxWeight)
				return false;
			for(int i = 0; i < this.inventory.length; i++) {
				if(inventory[i].getItem().isEmpty()) {
					inventory[i].setItem(item);
					return true;
				}
			}
			return false;
		}
	}

	public boolean addItems(Item... items) {
		for(Item i : items) {
			if(!addItem(i)) {
				return false;
			}
		}
		return true;
	}

	public boolean containsItem(Item i) {
		return getItems().contains(i);
	}

	/**
	 *
	 * @param luaItem An ItemReference to an item in an inventory.
	 * @return If the item was added to the inventory
	 */
	@Bind(SecurityLevel.DEFAULT) public Boolean putItem(LuaValue luaItem) {
		return addItem(ItemReference.class.cast(luaItem.checktable().get("this").checkuserdata(ItemReference.class))
				.derefItem());
	}

	/**
	 *
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT) public Integer size() {
		return this.maxSize;
	}

	/**
	 *
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT)
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
	@Bind(SecurityLevel.DEFAULT)
	public Varargs unpack() {
		final LuaValue[] ans = new LuaValue[inventory.length];
		IntStream.range(0, inventory.length)
				.forEach(i -> ans[i] = inventory[i].getLuaValue());
		return LuaValue.varargsOf(ans);
	}

	/**
	 *
	 */
	public void reset() {
		Stream.of(inventory).forEach(itemRef ->
				itemRef.setItem(new Item.EmptyItem()));
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
}
