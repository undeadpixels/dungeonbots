package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("An ItemChest is an Entity that contains a large Inventory.\n" +
		"The ItemChest can be locked or unlocked based off of events or by unlocking\n" +
		"with a key.")
public class ItemChest extends SpriteEntity implements HasInventory {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Inventory inventory = new Inventory(this, 100);

	public ItemChest(World world, String name, TextureRegion tex) {
		super(world, name, tex, new UserScriptCollection());
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
