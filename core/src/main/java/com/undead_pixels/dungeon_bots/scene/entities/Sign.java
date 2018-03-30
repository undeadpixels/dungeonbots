package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.Inspectable;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

@Doc("An Entity type that can be inspected ")
public class Sign extends SpriteEntity implements Inspectable {
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Decor0.png", 1, 5);
	private String message;
	public Sign(World world, String message, float x, float y) {
		super(world, "sign", DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
		this.message = message;
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public float getZ() {
		return 10f;
	}

	@Override
	public String inspect() {
		return message;
	}

	@Bind(value = SecurityLevel.ENTITY, doc = "")
	public
}
