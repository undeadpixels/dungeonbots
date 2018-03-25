package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

@Doc("An ItemChest is an Entity that contains a large Inventory.\n" +
		"The ItemChest can be isLocked or unlocked based off of events or by unlocking\n" +
		"with a key.")
public class ItemChest extends SpriteEntity implements HasInventory, Lockable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private volatile boolean locked = false;

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
	@Bind(value=SecurityLevel.AUTHOR, doc = "Returns true if ItemChest is isLocked, false otherwise.")
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	@Bind(value=SecurityLevel.AUTHOR, doc = "Set the ItemChest to a isLocked state.")
	public void lock() {
		this.locked = true;
	}

	@Override
	@Bind(value=SecurityLevel.AUTHOR, doc = "Set the ItemChest to an unlocked state.")
	public void unlock() {
		this.locked = false;
	}

	@Override
	public Boolean useItem(ItemReference item) {
		if(item.getItem().getClass() == Key.class) {
			item.derefItem();
			unlock();
			return true;
		}
		return false;
	}

	@Override
	public Boolean giveItem(ItemReference item) {
		return this.locked && this.inventory.addItem(item.derefItem());
	}

	@Override
	public Boolean canTake() {
		return !this.locked;
	}
}
