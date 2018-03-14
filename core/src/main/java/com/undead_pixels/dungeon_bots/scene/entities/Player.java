package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Note;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure.Gold;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.Sword;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
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
@Doc("A Player is an Actor afforded with more privileges")
public class Player extends RpgActor {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Deprecated
	protected String defaultCode;

	/**
	 * Constructor
	 * @param world The world this player belongs to
	 * @param name The name of this player
	 */
	public Player(World world, String name) {
		super(world, name, AssetManager.getTextureRegion("DawnLike/Characters/Player0.png", 3, 1), world.getPlayerTeamScripts());

		//world.getDefaultWhitelist().addAutoLevelsForBindables(this);
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
	public static Player newPlayer(
			@Doc("The assigned World") LuaValue world,
			@Doc("The X position of the player") LuaValue x,
			@Doc("The Y Position of the player") LuaValue y) {
		World w = (World) world.checktable().get("this").checkuserdata(World.class);
		Player p = w.getPlayer();
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

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.PLAYER;
	}
	
	public void resetInventory() {
		this.inventory.reset();
		this.inventory.addItems(
				new Note(this.world,"Welcome to Dungeonbots!"),
				new Website(this.world, "Youtube", "https://www.youtube.com/"),
				new MultipleChoiceQuestion(this.world, "What is the correct answer", "This", "or this"),
				new ResponseQuestion(this.world, "What's your favorite?", "Movie", "Game"),
				new Sword(this.world),
				new Gold(this.world, 25));
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
	}

	@Override
	public Boolean useItem(ItemReference ir) {
		Item item = ir.getItem();
		if(item.applyTo(this)) {
			ir.derefItem();
		}
		return true;
	}
}
