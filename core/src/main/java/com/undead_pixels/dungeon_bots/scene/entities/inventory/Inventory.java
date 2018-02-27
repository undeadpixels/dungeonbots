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

	final Entity owner;
	final Item[] inventory;
	private final int maxSize;
	private final int maxWeight = 100;

	public Inventory(final Entity owner, int maxSize) {
		this.owner = owner;
		this.maxSize = maxSize;
		inventory = new Item[maxSize];
		Whitelist w;//TODO
		w.addAutoLevelsForBindables(ItemReference.class);
	}

	public Inventory(final Entity entity, final Item[] items) {
		this.owner = entity;
		this.maxSize = 25;
		this.inventory = items;
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
		ItemReference ir = new ItemReference(this, i);
		owner.getSandbox().getWhitelist().add(ir);
		return ir;
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
		LuaTable table = new LuaTable();
		for(int i = 0; i < inventory.length; i++) {
			ItemReference ir = new ItemReference(this, i);
			this.owner.getSandbox().getWhitelist().add(ir);
			table.set(i + 1, ir.getLuaValue());
		}
		return table;
	}

	@Bind(SecurityLevel.DEFAULT)
	public Varargs unpack() {
		LuaValue[] ans = new LuaValue[inventory.length];
		IntStream.range(0, inventory.length).forEach(i -> {
			ItemReference ir = new ItemReference(this, i);
			this.owner.getSandbox().getWhitelist().add(ir);
			ans[i] = ir.getLuaValue();
		});
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
}
