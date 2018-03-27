package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Useable;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;

import java.io.Serializable;

/**
 * A type representing an immaterial game item.
 */
public abstract class Item implements GetLuaFacade, Serializable, Useable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final World world;
	protected final String name;
	protected final String description;
	protected final int value;
	protected final int weight;

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


	@Bind(SecurityLevel.DEFAULT) public String getDescription() {
		return description;
	}

	@Bind(SecurityLevel.DEFAULT) public Integer getValue() {
		return value;
	}

	@Bind(SecurityLevel.DEFAULT) public Integer getWeight() {
		return weight;
	}

	@Bind(SecurityLevel.DEFAULT) public Boolean up() {
		return true;
	}

	@Bind(SecurityLevel.DEFAULT) public Boolean down() {
		return true;
	}


	@Bind(SecurityLevel.DEFAULT) public Boolean left() {
		return true;
	}

	@Bind(SecurityLevel.DEFAULT) public Boolean right() {
		return true;
	}

	public String getName() {
		return this.name;
	}

	public boolean isEmpty() {
		return this.getClass().equals(EmptyItem.class);
	}
}
