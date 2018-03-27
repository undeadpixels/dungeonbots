package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;

@Doc("Type that encapsulates descriptive information about an item queried from a peek method")
public class ItemInfo implements GetLuaFacade {

	@Bind(SecurityLevel.DEFAULT)
	private final String name;

	@Bind(SecurityLevel.DEFAULT)
	private final String description;

	@Bind(SecurityLevel.DEFAULT)
	private final Integer index;

	@Bind(SecurityLevel.DEFAULT)
	private final Integer weight;

	@Bind(SecurityLevel.DEFAULT)
	private final Integer value;

	public ItemInfo(String name, String description, Integer index, Integer weight, Integer value) {
		this.name = name;
		this.description = description;
		this.index = index;
		this.weight = weight;
		this.value = value;
	}

	@Bind(value=SecurityLevel.DEFAULT,doc = "Returns a string representation of the item")
	public String tostr() {
		return String.format("%d -> %s : %s\n\tWeight := %d\tValue := %d", index, name, description, weight, value);
	}

	@Override
	public int getId() {
		return this.hashCode();
	}

	@Override
	public String getName() {
		return name;
	}
}
