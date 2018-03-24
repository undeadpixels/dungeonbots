package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

@Doc("A Door is an entity that can be triggered to open by events or unlocked with Keys")
public class Door extends SpriteEntity implements Lockable, Useable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean solid = false;
	private Key key;
	private volatile boolean locked = false;

	public Door(World world, TextureRegion tex) {
		super(world, "door", tex, new UserScriptCollection());
	}

	@Override
	public boolean isSolid() {
		return this.solid;
	}

	@Override
	public float getZ() {
		return 10;
	}

	@Bind
	public Key genKey() {
		this.key = new Key(
				this.world,
				getName() + "key",
				String.format("A key that unlocks the door for %s", getName()));
		return this.key;
	}

	@Override
	public Boolean useItem(ItemReference itemRef) {
		if(itemRef.getItem().getClass() == Key.class) {
			itemRef.derefItem();
			this.unlock();
			return true;
		}
		return false;
	}

	@Override
	@Bind(value=SecurityLevel.AUTHOR, doc="Returns true if Door is locked, false otherwise.")
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	@Bind(value=SecurityLevel.AUTHOR, doc="Sets the Door to a locked state.")
	public void lock() {
		this.locked = true;
	}

	@Override
	@Bind(value=SecurityLevel.AUTHOR, doc="Sets the Door to an unlocked state.")
	public void unlock() {
		this.locked = false;
	}

	@Override
	@Bind(value=SecurityLevel.DEFAULT, doc="Toggle the open state of the door depending on if it is locked.")
	public Boolean use() {
		return toggleOpen();
	}

	private boolean toggleOpen() {
		if(!locked) {
			this.solid = !this.solid;
			// Do something that changes the sprite to an 'open door' sprite
			return true;
		}
		return false;
	}
}
