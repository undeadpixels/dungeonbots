package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

@Doc("A Door is an entity that can be triggered to open by events or unlocked with Keys")
public class Door extends Actor implements Lockable, Useable, HasInventory {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Door0.png", 0, 0);

	private final TextureRegion defaultTexture;
	private final TextureRegion lockedTexture;
	private final TextureRegion openTexture;

	private volatile boolean open = false;
	private volatile boolean locked = false;
	private Inventory inventory;

	public Door(World world, float x, float y) {
		super(world, "door", DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
		final Tile UP = world.getTile(x, y + 1f);
		final Tile DOWN = world.getTile(x, y -1f);
		final boolean IS_UP = UP == null || UP.isSolid();
		final boolean IS_DOWN = DOWN == null || DOWN.isSolid();
		final boolean UP_DOWN = IS_UP || IS_DOWN;
		defaultTexture = AssetManager.getTextureRegion("DawnLike/Objects/Door0.png", UP_DOWN ? 1 : 0 , 0);
		lockedTexture = AssetManager.getTextureRegion("DawnLike/Objects/Door0.png", UP_DOWN ? 3 : 2 , 0);
		openTexture = AssetManager.getTextureRegion("DawnLike/Objects/Door1.png", UP_DOWN ? 1 : 0 , 0);
		sprite.setTexture(defaultTexture);
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.ENTITY, doc = "Create a new Door instance")
	public static Door create(
			@Doc("The World the Door belongs to") LuaValue world,
			@Doc("The X position of the door") LuaValue x,
			@Doc("The Y position of the door") LuaValue y) {
		return new Door(
				userDataOf(World.class, world),
				x.tofloat(),
				y.tofloat());
	}

	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		sandbox.registerEventType("ITEM_GIVEN", "Called when this Door is given an item", "item"); // TODO
		sandbox.registerEventType("LOCK", "Called when this Door is locked");
		sandbox.registerEventType("UNLOCK", "Called when this Door is unlocked");
		sandbox.registerEventType("OPEN", "Called when this Door is opened");
		sandbox.registerEventType("CLOSE", "Called when this Door is closed");
		sandbox.registerEventType("ENTER", "Called when an entity enters this Door", "entity");
		world.listenTo(World.EntityEventType.ENTITY_MOVED, this, (e) -> {
			if(e.getPosition().distance(this.getPosition()) < .1) {
				getSandbox().fireEvent("ENTER", e.getLuaValue());
			}
		}); 
	
		return sandbox;
	}

	@Override
	public boolean isSolid() {
		return !this.open;
	}

	@Override
	public float getZ() {
		return 10;
	}

	@Deprecated
	@Bind(SecurityLevel.AUTHOR)
	public Key genKey() {
		Key key = new Key(
				this.world,
				getName() + "key",
				String.format("A key that unlocks the door for %s", getName()));
		return key;
	}

	@Override
	public Boolean useItem(ItemReference itemRef) {
		if(itemRef.getItem() instanceof Key && isLocked()) {
			itemRef.derefItem();
			this.unlock();
			return true;
		}
		return false;
	}

	@Override
	@Bind(value=SecurityLevel.ENTITY, doc = "Returns true if Door is locked, false otherwise.")
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	@Bind(value=SecurityLevel.ENTITY, doc = "Sets the Door to a locked state.")
	public void lock() {
		getSandbox().fireEvent("LOCK");
		this.locked = true;
		this.open = false;
		this.sprite.setTexture(lockedTexture);
	}

	@Override
	@Bind(value=SecurityLevel.ENTITY, doc = "Sets the Door to an unlocked state.")
	public void unlock() {
		getSandbox().fireEvent("UNLOCK");
		this.locked = false;
		if(!this.open)
			this.sprite.setTexture(defaultTexture);
	}

	@Override
	@Bind(value=SecurityLevel.DEFAULT, doc = "Toggle the open state of the door depending on if it is locked.")
	public Boolean use() {
		return toggleOpen();
	}

	@Bind(value=SecurityLevel.ENTITY, doc = "Toggles the open state of the door")
	public Boolean toggleOpen() {
		if(!locked) {
			this.open = !this.open;
			this.sprite.setTexture(this.open ? openTexture : defaultTexture);
			this.world.updateEntity(this);
			
			if(this.open) {
				getSandbox().fireEvent("OPEN");
			} else {
				getSandbox().fireEvent("CLOSE");
			}
			
			return true;
		}
		return false;
	}

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	@Override
	public String inspect() {
		return String.format("A %s Door", locked ? "locked" : open ? "open" : "closed");
	}

	/* (non-Javadoc)
	 * @see com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory#getInventory()
	 */
	@Override
	public Inventory getInventory () {
		if(inventory == null) {
			inventory = new Inventory(this, 1);
		}
		return inventory;
	}

	@Override
	public Boolean canTake() {
		return true;
	}
}
