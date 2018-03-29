package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Note;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.awt.geom.Point2D;

import org.luaj.vm2.LuaValue;

/**
 * An Actor intended to be scripted and controlled by player users in a code
 * REPL or Editor
 * 
 * @author Stewart Charles
 * @version 1.0
 */
@Doc("A Player is an Actor afforded with more privileges")
public class Player extends RpgActor {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Characters/Player0.png", 3, 1);

	@Deprecated
	protected String defaultCode;

	/**
	 * Constructor
	 * @param world The world this player belongs to
	 * @param name The name of this player
	 */
	public Player(World world, String name, float x, float y) {
		super(world, name, DEFAULT_TEXTURE, makePlayerScript(), x, y);
	}

	/**
	 * @return
	 */
	private static UserScriptCollection makePlayerScript () {
		UserScriptCollection ret = new UserScriptCollection();
		ret.add(new UserScript("init", "--TODO")); // TODO
		return ret;
	}

	/**
	 * Static LuaBinding used to generate new players
	 * @param world The world to assign to the player
	 * @param x The initial x position of the player
	 * @param y The initial y position of the player
	 * @return A newly constructed Player that has been coerced into it's<br>
	 * associated LuaValue
	 */
	@Bind(SecurityLevel.AUTHOR)
	@BindTo("new")
	@Doc("Assigns a new player")
	@Deprecated
	public static Player newPlayer(
			@Doc("The assigned World") LuaValue world,
			@Doc("The X position of the player") LuaValue x,
			@Doc("The Y Position of the player") LuaValue y) {
		World w = (World) world.checktable().get("this").checkuserdata(World.class);
		Player p = new Player(w, "player", (float) x.checkdouble() - 1.0f, (float) y.checkdouble() - 1.0f);
		p.steps = 0;
		p.bumps = 0;
		w.addEntity(p);
		return p;
	}

	@Bind(SecurityLevel.AUTHOR)
	public void setDefaultCode(LuaValue df) {
		defaultCode = df.tojstring();
	}

	public String getDefaultCode() {
		return defaultCode != null ? defaultCode : "";
	}

	public void setPosition(Point2D.Float v) {
		sprite.setX(v.x);
		sprite.setY(v.y);
	}

	public int getSteps() {
		return steps;
	}

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.PLAYER;
	}
	
	public void resetInventory() {
		this.inventory.reset();
		this.inventory.addItem(new Note(this.world,"Welcome to Dungeonbots!"));
	}

	/**
	 *
	 * @param luaDir
	 * @param itemReference
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT)
	public Boolean use(LuaValue luaDir, LuaValue itemReference) {
		String dir = luaDir.checkjstring().toUpperCase();
		ItemReference itemRef = (ItemReference) itemReference.checktable().get("this")
				.checkuserdata(ItemReference.class);
		Direction direction = Direction.valueOf(dir);
		return false;
		//return this.world.tryUse(itemRef, direction, this);
	}

	@Override
	public LuaSandbox getSandbox() {
		LuaSandbox ret = super.getSandbox();
		ret.addBindable("player", this);
		return ret;
	}

}
