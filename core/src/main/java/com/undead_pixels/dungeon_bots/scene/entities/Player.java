package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Note;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
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
public class Player extends RpgActor {


	protected String defaultCode;

	/**
	 * Constructor
	 * 
	 * @param world
	 *            The world this player belongs to
	 * @param name
	 *            The name of this player
	 */
	public Player(World world, String name) {
		super(world, name, AssetManager.getAsset("player", AssetManager.AssetSrc.Player, 3, 1).orElse(null));
	}

	/**
	 * Static LuaBinding used to generate new players
	 * 
	 * @param world
	 *            The world to assign to the player
	 * @param x
	 *            The initial x position of the player
	 * @param y
	 *            The initial y position of the player
	 * @return A newly constructed Player that has been coerced into it's
	 *         associated LuaValue
	 */
	@Bind
	@BindTo("new")
	public static Player newPlayer(LuaValue world, LuaValue x, LuaValue y) {
		World w = (World) world.checktable().get("this").checkuserdata(World.class);
		Player p = w.getPlayer();
		SecurityContext.getWhitelist().add(p);
		p.steps = 0;
		p.bumps = 0;
		p.sprite.setX((float) x.checkdouble() - 1.0f);
		p.sprite.setY((float) y.checkdouble() - 1.0f);
		return p;
	}

	@Bind(SecurityLevel.DEFAULT)
	public void tryAgain() {
		world.reset();
	}
	/**
	 * Used to create a non-useful player to display in the Level Editor's
	 * palette.
	 */
	public static Player worldlessPlayer() {
		return new Player(null, "A player");
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

	public void resetInventory() {
		this.inventory.reset();
		this.inventory.addItem(new Note("Greetings", "Welcome to Dungeonbots!"));
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

}
