package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.actions.Action;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

/**
 * An Actor intended to be scripted and controlled by player users in a code
 * REPL or Editor
 * 
 * @author Stewart Charles
 * @version 1.0
 */
public class Player extends RpgActor {

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
		p.sprite.setX((float) x.checkdouble() - 1.0f);
		p.sprite.setY((float) y.checkdouble() - 1.0f);
		return p;
	}

	@Bind
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

	public void setPosition(Vector2 v) {
		sprite.setX(v.x);
		sprite.setY(v.y);
	}

}
