package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

@Doc("A Key Entity is a Key Item that is also an entity")
public class KeyEntity extends ItemEntity {
	private static final long serialVersionUID = 1L;
	public static final TextureRegion KEY_TEXTURE = AssetManager.getTextureRegion("DawnLike/Items/Key.png", 0, 0);

	public KeyEntity(World world, float x, float y) {
		super(world, "key", KEY_TEXTURE, new Key(world,"Key","A Key that can open a Door or Item Chest"));
		this.sprite.setPosition(x,y);
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Key Entity that exists in the game map")
	public static KeyEntity create(
			@Doc("The World that the Key Item belongs to") LuaValue world,
			@Doc("The X position of the Key Item") LuaValue x,
			@Doc("The Y position of the Key Item") LuaValue y) {
		return new KeyEntity(
				(World)world.checktable().get("this").checkuserdata(World.class),
				x.tofloat(),
				y.tofloat());
	}

}
