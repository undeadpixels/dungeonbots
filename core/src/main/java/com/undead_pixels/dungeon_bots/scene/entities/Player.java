package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Note;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.*;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.awt.geom.Point2D;
import java.util.concurrent.locks.ReentrantLock;

import org.luaj.vm2.LuaValue;

/**
 * An Actor intended to be scripted and controlled by player users in a code
 * REPL or Editor
 *
 * @author Stewart Charles
 * @version 1.0
 */
@Doc("A Player is an Actor afforded with more privileges")
public class Player extends RpgActor implements Pushable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Characters/Player0.png", 3, 1);

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
		ret.add(new UserScript("init", "registerKeyPressedListener(function(k)\n"
				+ "  if k==\"up\" then this:up() end\n"
				+ "  if k==\"down\" then this:down() end\n"
				+ "  if k==\"left\" then this:left() end\n"
				+ "  if k==\"right\" then this:right() end\n"
				+ "  \n"
				+ "  if k==\"w\" then this:up() end\n"
				+ "  if k==\"s\" then this:down() end\n"
				+ "  if k==\"a\" then this:left() end\n"
				+ "  if k==\"d\" then this:right() end\n"
				+ "end)\n\n"));
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
	@BindTo("new")
	@Doc("Assigns a new player")
	@Deprecated
	@Bind(SecurityLevel.AUTHOR)
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

	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		sandbox.registerEventType("KEY_PRESSED", "Called when a key is pressed on the keyboard", "key"); // TODO - make repeating/coalescing
		sandbox.registerEventType("KEY_RELEASED", "Called when a key is released on the keyboard", "key");
		world.listenTo(World.StringEventType.KEY_PRESSED, this, (s) -> {
			sandbox.fireEvent("KEY_PRESSED", LuaValue.valueOf(s));
		});
		world.listenTo(World.StringEventType.KEY_RELEASED, this, (s) -> {
			sandbox.fireEvent("KEY_RELEASED", LuaValue.valueOf(s));
		});
		sandbox.registerEventType("BUMPED", "Called when this player is bumped into", "Entity that bumps", "direction");
		return sandbox;
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

	}

	@Override
	public Boolean useItem(ItemReference itemRef) {
		return this.inventory.tryTakeItem(itemRef) >= 0;
	}

	@Override
	public LuaSandbox getSandbox() {
		LuaSandbox ret = super.getSandbox();
		ret.addBindable("player", this);
		return ret;
	}

	@Override
	public String inspect() {
		return "Player";
	}

	@Override
	public void push(Direction direction) {
		// do nothing
	}
	transient volatile Player requestFrom = null;
	transient volatile Player requestTo = null;
	@Bind(value = SecurityLevel.NONE, doc = "Requests to trade items with an entity. Completes trade if both entities request a trade.")
	public void requestTrade(@Doc("The target Player to trade with") LuaValue target) {
		final Player test = (Player) target.checktable().get("this").checkuserdata(Entity.class);
		synchronized (Player.class) {
			if (test == this || test == requestTo) {
				resetRequest();
				return;
			}
			requestTo = test;
			if (requestTo == requestFrom) {
				this.inventory.swapInventory(requestTo.inventory);
				world.message(this,
						String.format("%s traded inventories with %s", this.name, requestTo.name),
						LoggingLevel.GENERAL);
				requestTo.resetRequest();
				resetRequest();
			} else
				requestTo.requestFrom = this;
		}
	}

	private void resetRequest() {
		requestTo = null;
		requestFrom = null;
	}

	@Override
	public void bumpedInto(Entity e, Direction direction) {
		getSandbox().fireEvent("BUMPED", e.getLuaValue(), LuaValue.valueOf(direction.name()));
		world.message(this, String.format("Bumped %s", direction.name().toLowerCase()), LoggingLevel.GENERAL);
	}

}
