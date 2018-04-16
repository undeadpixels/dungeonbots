package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;

@Doc("Type that encapsulates descriptive information about an item queried from a peek method")
public class ItemInfo implements GetLuaFacade {

	private final String name;

	private final String description;

	private final Integer index;

	private final Integer weight;

	private final Integer value;

	public ItemInfo(String name, String description, Integer index, Integer weight, Integer value) {
		this.name = name;
		this.description = description;
		this.index = index;
		this.weight = weight;
		this.value = value;
	}

	@Bind(value=SecurityLevel.DEFAULT,doc = "Returns a string representation of the item")
	public String toString() {
		return String.format("%d -> %s : %s\n\tWeight := %d\tValue := %d", index, name, description, weight, value);
	}

	@BindTo("name")
	@Bind(value=SecurityLevel.NONE, doc = "Get the Name of the Item")
	public String getName() {
		return name;
	}

	@BindTo("description")
	@Bind(value=SecurityLevel.NONE, doc = "Get the Description of the Item")
	public String getDescription() {
		return description;
	}

	@BindTo("index")
	@Bind(value=SecurityLevel.NONE, doc = "Get the Index of the Item")
	public Integer getIndex() {
		return index;
	}

	@BindTo("weight")
	@Bind(value=SecurityLevel.NONE, doc = "Get the Weight of the Item")
	public Integer getWeight() {
		return weight;
	}

	@BindTo("value")
	@Bind(value=SecurityLevel.NONE, doc = "Get the Value of the Item")
	public Integer getValue() {
		return value;
	}
}
