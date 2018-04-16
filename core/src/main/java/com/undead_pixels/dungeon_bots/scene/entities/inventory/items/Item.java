package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ApplyItem;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.LuaValue;

import java.io.Serializable;

/**
 * A type representing an immaterial game item.
 */
@Doc("An Item is a type representing an immaterial game item that is carried in an Inventory.")
public abstract class Item implements GetLuaFacade, Serializable, ApplyItem {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final World world;
	protected String name;
	protected String description;
	protected int value;
	protected int weight;

	/**
	 * A Static Empty Item class used for default Inventory Slots
	 */
	public static class EmptyItem extends Item {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public EmptyItem() {
			super(null, "Empty", "An Empty Item Slot", 0, 0);
		}
	}

	public Item(World w, String name, String descr, int value, int weight) {
		this.world = w;
		this.name = name;
		this.description = descr;
		this.value = value;
		this.weight = weight;
	}

	@Doc("Get the Description of the Item")
	@Bind(SecurityLevel.AUTHOR) public String getDescription() {
		return description;
	}

	@Doc("Get the Value of the Item")
	@Bind(SecurityLevel.AUTHOR) public Integer getValue() {
		return value;
	}

	@Doc("Get the Weight of the Item")
	@Bind(SecurityLevel.AUTHOR) public Integer getWeight() {
		return weight;
	}

	@Override public Boolean applyTo(Entity entity) {
		return false;
	}

	@Doc("Get the Name of the Item")
	public String getName() {
		return this.name;
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Set the Name of the Item")
	public void setName(LuaValue name) {
		this.name = name.checkjstring();
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Set the description of the Item")
	public void setDescription(LuaValue description) {
		this.description = description.checkjstring();
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Set the Value of the Item")
	public void setValue(LuaValue value) {
		this.value = value.toint();
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Set the Weight of the Item")
	public void setWeight(LuaValue weight) {
		this.weight = weight.toint();
	}

	public boolean isEmpty() {
		return this.getClass().equals(EmptyItem.class);
	}
}
