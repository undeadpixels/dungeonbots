/**
 * 
 */
package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

/**
 * @author kevin
 *
 */
public class OtherSign extends SpriteEntity {

	private static final long serialVersionUID = 1L;

	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Decor0.png", 1, 5);
	
	private String text;

	/**
	 * @param world
	 * @param name
	 * @param x
	 * @param y
	 */
	public OtherSign(World world, String name, float x, float y, String text) {
		super(world, name, DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
	}

	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Sign Instance")
	public static OtherSign create(
			@Doc("The world the Goal belongs to") LuaValue world,
			@Doc("The name of the sign") LuaValue string,
			@Doc("The X position of the Sign") LuaValue x,
			@Doc("The Y position of the Sign") LuaValue y,
			@Doc("The text of the sign") LuaValue text) {
		return new OtherSign(
				(World)world.checktable().get("this").checkuserdata(World.class),
				string.checkjstring(),
				x.tofloat(),
				y.tofloat(),
				text.tojstring());
	}

	/* (non-Javadoc)
	 * @see com.undead_pixels.dungeon_bots.scene.BatchRenderable#getZ()
	 */
	@Override
	public float getZ () {
		return 1;
	}

	/* (non-Javadoc)
	 * @see com.undead_pixels.dungeon_bots.scene.entities.Entity#isSolid()
	 */
	@Override
	public boolean isSolid () {
		return false;
	}
	
	@Bind(SecurityLevel.AUTHOR)
	public void setText(LuaValue text) {
		setText(text.checkjstring());
	}
	
	public void setText(String text) {
		this.text = text;
	}

	@Bind(SecurityLevel.NONE)
	public LuaValue text() {
		return LuaValue.valueOf(text);
	}
	
	public String getText() {
		return text;
	}
	
}
