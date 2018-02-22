package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.LuaValue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A type that encapsulates handling accessing and modifying
 * an inventory for an entity.
 */
public class Inventory implements GetLuaFacade {

	final Item[] inventory;
	private final int maxSize;
	private final int maxWeight = 100;

	public Inventory(int maxSize) {
		this.maxSize = maxSize;
		inventory = new Item[maxSize];
	}

	public Inventory(final Item[] items) {
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
	@Bind(SecurityLevel.DEFAULT) public ItemReference peekItem(LuaValue index) {
		final int i = index.checkint() - 1;
		assert i < this.inventory.length;
		return new ItemReference(this, i - 1);
	}


	public Item removeItem(int i) {
		assert i < this.inventory.length;
		synchronized (this.inventory) {
			Item item = this.inventory[i - 1];
			this.inventory[i - 1] = null;
			return item;
		}
	}

	public boolean addItem(Item item) {
		synchronized (this.inventory) {
			for(int i = 0; i < this.inventory.length; i++) {
				if(inventory[i] == null) {
					inventory[i] = item;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *
	 * @param luaItem
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT) public Boolean putItem(LuaValue luaItem) {
		Item item = (Item) luaItem.checktable().get("this").checkuserdata(Item.class);
		return addItem(item);
	}

	@Bind(SecurityLevel.DEFAULT) public Integer capacity() {
		return this.maxSize;
	}

	@Bind(SecurityLevel.DEFAULT) public Integer currentCapacity() {
		int current = 0;
		for(Item i : inventory)
			current += i == null ? 0 : 1;
		return current;
	}
}
