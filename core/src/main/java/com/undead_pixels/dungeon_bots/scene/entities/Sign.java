package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.Inspectable;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

@Doc("An Entity type that can be inspected ")
public class Sign extends SpriteEntity implements Inspectable {
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Decor0.png", 1, 5);
	private String message;

	public Sign(World world, String message, float x, float y) {
		super(world, "sign", DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
		this.message = message;
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Sign")
	public static Sign create(
			@Doc("The World the Sign belongs to") LuaValue world,
			@Doc("The Message the Sign should display") LuaValue message,
			@Doc("The X position of the Sign") LuaValue x,
			@Doc("The Y position of the Sign") LuaValue y) {
		return new Sign(userDataOf(World.class, world), message.checkjstring(), x.tofloat(), y.tofloat());
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

	@Bind(value = SecurityLevel.ENTITY, doc = "Change the message that the sign displays")
	public void setMessage(@Doc("The new message that should display") LuaValue message) {
		this.message = message.checkjstring();
	}
}
