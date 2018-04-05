package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

public class Block extends Actor implements Pushable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final TextureRegion DEFAULT_TEXTURE =
			AssetManager.getTextureRegion("DawnLike/Objects/Tile.png", 3, 0);

	private Boolean isMoveable = true;

	public Block(World world, float x, float y) {
		super(world, "block", DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
		if(isMoveable) {
			this.getScripts().add(new UserScript("init",
					"registerBumpedListener(function(e, dir)\n"
					+ "  this:move(dir)\n"
					+ "end)\n"));
		}
	}

	/**
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @return
	 */
	@BindTo("new")
	@Bind(value = SecurityLevel.TEAM, doc = "Create a new Block")
	public static Block create(
			@Doc("The World that contains the Block") LuaValue world,
			@Doc("The X position of the Block") LuaValue x,
			@Doc("The Y position of the Block") LuaValue y) {
		return new Block(userDataOf(World.class, world), x.tofloat(), y.tofloat());
	}

	/**
	 *
	 * @param movable
	 * @return
	 */
	@Bind(value = SecurityLevel.ENTITY, doc = "Set the entity to a movable state with a boolean flag")
	public Block setMovable(
			@Doc("A Boolean: True if Block should be moveable, false otherwise") LuaValue movable) {
		this.isMoveable = movable.checkboolean();
		return this;
	}
	
	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		sandbox.registerEventType("PUSH");
		sandbox.registerEventType("BUMPED");
	
		return sandbox;
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
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	@Override
	public void push(final Actor.Direction direction) {
		if(isMoveable) {
			queueMoveSlowly(direction, true);
		}
		
		getSandbox().fireEvent("PUSH", LuaValue.valueOf(direction.name()));
	}
	
	public void bumpedInto(final Entity e, final Actor.Direction direction) {
		getSandbox().fireEvent("BUMPED", e.getLuaValue(), LuaValue.valueOf(direction.name()));
	}

	@Override
	public String inspect() {
		return "A Heavy Block of Ice. Perhaps you can push it?";
	}
}
