package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;

public abstract class ItemEntity extends SpriteEntity {

	private static final long serialVersionUID = 1L;
	protected Item item;

	public ItemEntity(World world, String name, TextureRegion tex, Item item) {
		super(world, name, tex, new UserScriptCollection());
		this.item = item;
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	public Item getItem() {
		return this.item;
	}

	@Override
	public float getZ() {
		return 15f;
	}

	public <T extends HasInventory> Boolean pickUp(final T dst) {
		if(dst.getInventory().addItem(this.item)) {
			world.removeEntity(this);
			this.item = null;
			return true;
		}
		return false;
	}
}
