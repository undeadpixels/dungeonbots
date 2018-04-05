/**
 * 
 */
package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
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
public class Goal extends SpriteEntity {

	private static final long serialVersionUID = 1L;

	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Door0.png", 3, 5);

	/**
	 * @param world
	 * @param name
	 * @param x
	 * @param y
	 */
	public Goal(World world, String name, float x, float y) {
		super(world, name, DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
	}

	@Bind(value = SecurityLevel.AUTHOR,
			doc = "Create a new Goal Instance")
	public static Goal create(
			@Doc("The world the Goal belongs to") LuaValue world,
			@Doc("The name of the goal") LuaValue string,
			@Doc("The X position of the Goal") LuaValue x,
			@Doc("The Y position of the Goal") LuaValue y) {
		return new Goal(
				(World)world.checktable().get("this").checkuserdata(World.class),
				string.checkjstring(),
				x.tofloat(),
				y.tofloat());
	}

	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		sandbox.registerEventType("ENTER");
		world.listenTo(World.EntityEventType.ENTITY_MOVED, this, (e) -> {
			if(e.getPosition().distance(this.getPosition()) < .1) {
				getSandbox().fireEvent("ENTER", e.getLuaValue());
			}
		}); 
	
		return sandbox;
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

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	@Override
	public String inspect() {
		return this.getClass().getSimpleName();
	}
}
