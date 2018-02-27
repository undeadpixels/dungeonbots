package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;

public class ItemChest extends SpriteEntity implements HasInventory {

	private final Inventory inventory = new Inventory(getSandbox(), 100);	

	public ItemChest(World world, String name, TextureRegion tex) {
		super(world, name, tex);
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	@Override
	public float getZ() {
		return 0;
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public Boolean useItem(ItemReference item) {
		return false;
	}

	@Override
	public Boolean giveItem(ItemReference item) {
		return false;
	}
}
