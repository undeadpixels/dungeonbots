package com.undead_pixels.dungeon_bots.scene.entities.inventory;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;

/**
 * A type representing an immaterial game item.
 */
public class Item implements GetLuaFacade {

	private final String name;
	private final String description;
	private final int value;
	private final int weight;

	public Item(String name, String descr, int value, int weight) {
		this.name = name;
		this.description = descr;
		this.value = value;
		this.weight = weight;
	}

	@Override
	public int getId() {
		return this.hashCode();
	}

	@Bind(SecurityLevel.DEFAULT) @Override public String getName() {
		return this.name;
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

}
