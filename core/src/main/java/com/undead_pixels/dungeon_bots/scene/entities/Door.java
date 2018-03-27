package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

@Doc("A Door is an entity that can be triggered to open by events or unlocked with Keys")
public class Door extends SpriteEntity implements Lockable, Useable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Door0.png", 0, 0);

	private boolean solid = false;
	private Key key;
	private volatile boolean locked = false;

	public Door(World world, float x, float y) {
		super(world, "door", DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Door instance")
	public static Door create(
			@Doc("The World the Door belongs to") LuaValue world,
			@Doc("The X position of the door") LuaValue x,
			@Doc("The Y position of the door") LuaValue y) {
		return new Door(
				(World)world.checktable().get("this").checkuserdata(World.class),
				x.tofloat(),
				y.tofloat());
	}

	@Override
	public boolean isSolid() {
		return this.solid;
	}

	@Override
	public float getZ() {
		return 10;
	}

	@Bind(SecurityLevel.AUTHOR)
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
	@Bind(value=SecurityLevel.AUTHOR, doc = "Returns true if Door is locked, false otherwise.")
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	@Bind(value=SecurityLevel.AUTHOR, doc = "Sets the Door to a locked state.")
	public void lock() {
		this.locked = true;
	}

	@Override
	@Bind(value=SecurityLevel.AUTHOR, doc = "Sets the Door to an unlocked state.")
	public void unlock() {
		this.locked = false;
	}

	@Override
	@Bind(value=SecurityLevel.DEFAULT, doc = "Toggle the open state of the door depending on if it is locked.")
	public Boolean use() {
		return toggleOpen();
	}

	@Bind(value=SecurityLevel.AUTHOR, doc="Toggles the open state of the door")
	public Boolean toggleOpen() {
		if(!locked) {
			this.solid = !this.solid;
			// Do something that changes the sprite to an 'open door' sprite
			return true;
		}
		return false;
	}
}
