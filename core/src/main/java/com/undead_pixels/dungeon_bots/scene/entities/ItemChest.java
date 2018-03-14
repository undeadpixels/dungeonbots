package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

public class ItemChest extends SpriteEntity implements HasInventory {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Items/Chest0.png", 1, 0);

	private final Inventory inventory = new Inventory(this, 100);

	public ItemChest(World world, String name, float x, float y) {
		super(world, name, DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
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
