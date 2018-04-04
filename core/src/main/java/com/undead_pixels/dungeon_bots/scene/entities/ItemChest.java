package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

@Doc("An ItemChest is an Entity that contains a large Inventory.\n" +
		"The ItemChest can be isLocked or unlocked based off of events or by unlocking\n" +
		"with a key.")
public class ItemChest extends SpriteEntity implements HasInventory, Lockable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final TextureRegion LOCKED_TEXTURE = AssetManager.getTextureRegion("DawnLike/Items/Chest0.png", 1, 0);
	public static final TextureRegion UNLOCKED_TEXTURE = AssetManager.getTextureRegion("DawnLike/Items/Chest1.png", 1, 0);

	private volatile boolean locked = false;

	private final Inventory inventory = new Inventory(this, 100);

	public ItemChest(World world, String name, float x, float y) {
		super(world, name, UNLOCKED_TEXTURE, new UserScriptCollection(), x, y);
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Creates a new ItemChest instance")
	@BindTo("new")
	public static ItemChest create(
			@Doc("The World the ItemChest belongs to") LuaValue world,
			@Doc("The name the ItemChest is bound to in it's script environment") LuaValue name,
			@Doc("The X position of the ItemChest") LuaValue x,
			@Doc("The Y position of the ItemChest") LuaValue y) {
		return new ItemChest(
				userDataOf(World.class, world),
				name.checkjstring(),
				x.tofloat(),
				y.tofloat());
	}
	
	@Override
	public void sandboxInit() {
		getSandbox().registerEventType("LOCK");
		getSandbox().registerEventType("UNLOCK");
		getSandbox().registerEventType("OPEN");
		getSandbox().registerEventType("CLOSE");
	
		super.sandboxInit();
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public float getZ() {
		return 0;
	}

	@Bind(value = SecurityLevel.ENTITY, doc ="")
	@BindTo("inventory")
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	@Bind(value=SecurityLevel.NONE, doc = "Returns true if ItemChest is isLocked, false otherwise.")
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	@Bind(value=SecurityLevel.ENTITY, doc = "Set the ItemChest to a isLocked state.")
	public void lock() {
		getSandbox().fireEvent("LOCK");
		this.locked = true;
		this.sprite.setTexture(LOCKED_TEXTURE);
	}

	@Override
	@Bind(value=SecurityLevel.ENTITY, doc = "Set the ItemChest to an unlocked state.")
	public void unlock() {
		getSandbox().fireEvent("UNLOCK");
		this.locked = false;
		this.sprite.setTexture(UNLOCKED_TEXTURE);
	}

	@Override
	public Boolean useItem(ItemReference item) {
		if(item.getItem() instanceof Key) {
			item.derefItem();
			unlock();
			return true;
		}
		return false;
	}

	@Override
	public Boolean giveItem(ItemReference item) {
		return !this.locked && this.inventory.addItem(item.derefItem());
	}

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	@Override
	public Boolean canTake() {
		return !this.locked;
	}

	@Override
	public String inspect() {
		return locked ? "A locked Item Chest" : "An unlocked Item Chest";
	}
}
