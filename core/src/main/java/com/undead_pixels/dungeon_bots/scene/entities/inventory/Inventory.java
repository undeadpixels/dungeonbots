package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.*;

import java.io.Serializable;
import java.util.stream.*;

/**
 * A type that encapsulates handling accessing and modifying
 * an inventory for an entity.
 */
public class Inventory implements GetLuaFacade, Serializable {

	/**
	 * The Entity that owns the inventory.
	 */
	private final Entity owner;

	/**
	 * The underlying array if Items of the inventory.
	 */
	final Item[] inventory;

	/**
	 *
	 */
	final ItemReference[] itemReferences;

	/**
	 * The max size of the inventory.
	 */
	private final int maxSize;

	/**
	 * The max weight of the inventory.
	 */
	private final int maxWeight = 100;

	public Inventory(final Entity owner, int maxSize) {
		this.owner = owner;
		this.maxSize = maxSize;
		inventory = new Item[maxSize];
		itemReferences = new ItemReference[maxSize];
		IntStream.range(0, maxSize).forEach(i -> itemReferences[i] = new ItemReference(this, i));
		owner.getWhitelist().addAutoLevelsForBindables(ItemReference.class);
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
		assert i < this.inventory.length && i < this.itemReferences.length;
		return itemReferences[i];
	}

	/**
	 *
	 * @param i
	 * @return
	 */
	public Item removeItem(int i) {
		assert i < this.inventory.length;
		synchronized (this.inventory) {
			Item item = this.inventory[i - 1];
			this.inventory[i - 1] = null;
			return item;
		}
	}

	/**
	 *
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Integer getWeight() {
		return Stream.of(inventory)
				.reduce(0, (num, item) -> num + (item == null ? 0 : item.getWeight()), (a, b) -> a + b);
	}

	/**
	 * Calculate the current capacity of the Inventory by calculating the number of non-null<br>
	 * items held within.
	 * @return An integer representing the number of items currently held in the inventory
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Integer currentCapacity() {
		return Stream.of(inventory)
				.reduce(0, (num, item) -> num + (item == null ? 0 : 1), (a,b) -> a + b);
	}

	/**
	 *
	 * @param item
	 * @return
	 */
	public boolean addItem(Item item) {
		synchronized (this.inventory) {
			if(getWeight() + item.getWeight() > maxWeight)
				return false;
			for(int i = 0; i < this.inventory.length; i++) {
				if(inventory[i] == null) {
					inventory[i] = item;
					return true;
				}
			}
			return false;
		}
	}

	/**
	 *
	 * @param luaItem An ItemReference to an item in an inventory.
	 * @return If the item was added to the inventory
	 */
	@Bind(SecurityLevel.DEFAULT) public Boolean putItem(LuaValue luaItem) {
		return ItemReference.class.cast(luaItem.checktable().get("this").checkuserdata(ItemReference.class))
				.derefItem()
				.map(item -> addItem(item))
				.orElse(false);
	}

	/**
	 *
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT) public Integer capacity() {
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
			ItemReference ir = itemReferences[i];
			table.set(i + 1, ir.getLuaValue());
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
				.forEach(i -> ans[i] = itemReferences[i].getLuaValue());
		return LuaValue.varargsOf(ans);
	}

	/**
	 *
	 */
	public void reset() {
		for(int i = 0; i < inventory.length; i++) {
			inventory[i] = null;
		}
	}

	/**
	 *
	 * @return
	 */
	public Item[] getInventory() {
		return inventory;
	}
}
