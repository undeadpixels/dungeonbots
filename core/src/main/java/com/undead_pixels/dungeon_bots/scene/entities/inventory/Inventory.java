package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.LuaValue;
import java.util.List;

/**
 * A type that encapsulates handling accessing and modifying
 * an inventory for an entity.
 */
public class Inventory implements GetLuaFacade {

	private final List<Item> inventory;

	public Inventory(final List<Item> items) {
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
	@Bind(SecurityLevel.DEFAULT) public Item peekItem(LuaValue index) {
		final int i = index.checkint() - 1;
		assert i < this.inventory.size();
		return this.inventory.get(index.checkint() - 1);
	}

	/**
	 *
	 * @param itemIndex
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT) public Item removeItem(LuaValue itemIndex) {
		final int i = itemIndex.checkint() - 1;
		assert i < this.inventory.size();
		synchronized (this.inventory) {
			return this.inventory.remove(i - 1);
		}
	}

	/**
	 *
	 * @param luaItem
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT) public Inventory putItem(LuaValue luaItem) {
		Item item = (Item) luaItem.checktable().get("this").checkuserdata(Item.class);
		synchronized (this.inventory) {
			this.inventory.add(item);
		}
		return this;
	}

	@Bind
}
