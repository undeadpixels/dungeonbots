package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.HasInventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure.Diamond;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure.Gem;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure.Gold;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

public class ItemEntity extends Actor implements HasInventory {

	private static final long serialVersionUID = 1L;
	private final Inventory inventory = new Inventory(this, 1);

	public ItemEntity(World world, String name, TextureRegion tex, Item item, float x, float y) {
		super(world, name, tex, new UserScriptCollection());
		this.inventory.addItem(item);
		this.sprite.setPosition(x, y);
		
		this.getScripts().add(new UserScript("init", "\n"
				+ "registerEnterListener(function(e)\n"
				+ "  e.grab()\n"
				+ "end)"));
	}

	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		
		sandbox.registerEventType("ENTER", "Called when another entity moves to the same tile as this", "entity");
		world.listenTo(World.EntityEventType.ENTITY_MOVED, this, (e) -> {
			if(e.getPosition().distance(this.getPosition()) < .1) {
				getSandbox().fireEvent("ENTER", e.getLuaValue());
			}
		}); 
		
		return sandbox;
	}

	@Override
	public boolean isSolid() {
		return false;
	}
	
	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	public Item getItem() {
		return this.inventory.getItems().get(0);
	}
	public void setItem(Item i) {
		this.inventory.reset();
		this.inventory.addItem(i);
	}

	@Override
	public float getZ() {
		return 15f;
	}

	public <T extends HasInventory> Boolean pickUp(final T dst) {
		if(dst.getInventory().addItem(getItem())) {
			world.removeEntity(this);
			this.inventory.reset();
			return true;
		}
		return false;
	}

	public static final TextureRegion KEY_TEXTURE = AssetManager.getTextureRegion("DawnLike/Items/Key.png", 0, 0);

	public static ItemEntity key(World world, float x, float y) {
		return new ItemEntity(world, "key", KEY_TEXTURE, new Key(world), x, y);
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Key Entity that exists in the game map")
	public static ItemEntity key(
			@Doc("The World that the Key Item belongs to") LuaValue world,
			@Doc("The X position of the Key Item") LuaValue x,
			@Doc("The Y position of the Key Item") LuaValue y) {
		return ItemEntity.key(
				(World)world.checktable().get("this").checkuserdata(World.class),
				x.tofloat(),
				y.tofloat());
	}

	public static final TextureRegion GOLD_TEXTURE = AssetManager.getTextureRegion("DawnLike/Items/Money.png", 0, 1);
	public static ItemEntity gold(World world, float x, float y, int weight) {
		return new ItemEntity(world, "gold" ,GOLD_TEXTURE, new Gold(world,weight),  x, y);
	}
	
	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new GoldItem")
	public static ItemEntity gold(
			@Doc("The World the GoldItem belongs to") LuaValue world,
			@Doc("The X position of the GoldItem") LuaValue x,
			@Doc("The Y position of the GoldItem") LuaValue y,
			@Doc("The Weight of the GoldItem") LuaValue weight) {
		return ItemEntity.gold(userDataOf(World.class, world), x.tofloat(), y.tofloat(), weight.toint());
	}

	public static final TextureRegion DIAMOND_TEXTURE = AssetManager.getTextureRegion("DawnLike/Items/Money.png", 1, 2);
	public static ItemEntity diamond(World world, float x, float y) {
		return new ItemEntity(world, "diamond", DIAMOND_TEXTURE, new Diamond(world), x, y);
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new DiamondEntity")
	public static ItemEntity diamond(
			@Doc("The World the DiamondEntity belongs to") LuaValue world,
			@Doc("The X position of the DiamondEntity") LuaValue x,
			@Doc("The Y position of the DiamondEntity") LuaValue y) {
		return ItemEntity.diamond(userDataOf(World.class, world), x.tofloat(), y.tofloat());
	}

	public static final TextureRegion GEM_TEXTURE = AssetManager.getTextureRegion("DawnLike/Items/Money.png", 6, 2);
	public static ItemEntity gem(World world, float x, float y) {
		return new ItemEntity(world, "gem", GEM_TEXTURE, new Gem(world), x, y);
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Gem Entity")
	public static ItemEntity gem(
			@Doc("The World the GemEntity belongs to") LuaValue world,
			@Doc("The X position of the GemEntity") LuaValue x,
			@Doc("The Y position of the GemEntity") LuaValue y) {
		return ItemEntity.gem(userDataOf(World.class, world),x.tofloat(), y.tofloat());
	}

	@Override
	@BindTo("inventory")
	@Bind(value = SecurityLevel.ENTITY, doc = "Get the ItemEntity's inventory")
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public String inspect() {
		return String.format(
				"%s %s",
				getItem().getName(),
				getItem().getDescription());
	}
}
