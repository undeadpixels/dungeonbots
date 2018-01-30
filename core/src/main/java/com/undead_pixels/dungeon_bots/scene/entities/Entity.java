package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionQueue;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;

/**
 * Pretty much everything visible/usable within a regular game. Does not include UI elements.
 */
public abstract class Entity implements BatchRenderable, Scriptable, GetBindable {

	/**
	 * A user sandbox that is run on this object
	 */
	protected LuaSandbox sandbox = new LuaSandbox();
	
	/**
	 * A string representing this Entity's script (if any)
	 */
	protected String scriptText;
	
	/**
	 * The queue of actions this Entity is going to take
	 */
	protected ActionQueue actionQueue = new ActionQueue(this);
	
	/**
	 * The world of which this Entity is a part
	 */
	protected final World world;
	
	/**
	 * Some simple int that can uniquely identify this entity
	 */
	protected final int id;
	
	/**
	 * A name for this entity that can potentially be user-facing
	 */
	protected final String name;

	/**
	 * @param world		The world to contain this Actor
	 */
	public Entity(World world, String name) {
		this(world, name, null);
	}

	public Entity(World world, String name, Whitelist whitelist) {
		this.world = world;
		this.name = name;
		this.id = world.makeID();
		this.world.addEntity(this);
		this.sandbox.addBindable(this);
	}

	@Override
	public LuaSandbox getSandbox() {
		return sandbox;
	}
	
	public void update(float dt) {
		// TODO - sandbox.resume();
		actionQueue.act(dt);
	}
	
	/**
	 * @param sandbox		The user sandbox to set
	 */
	public void setSandbox(LuaSandbox sandbox) {
		this.sandbox = sandbox;
	}

	@SafeVarargs
	public final <T extends GetBindable> Entity addToSandbox(T... vals) {
		this.sandbox.addBindable(vals);
		return this;
	}
	
	/**
	 * @return		This Entity's position in tile space
	 */
	public abstract Vector2 getPosition();
	
	
	/**
	 * @return		If this object disallows movement through it
	 */
	public abstract boolean isSolid();

	/**
	 * TODO - should this be private?
	 * 
	 * @return	This Entity's action queue
	 */
	public ActionQueue getActionQueue() {
		return actionQueue;
	}
	
	/**
	 * @return	The team of this Entity
	 */
	public TeamFlavor getTeam() {
		return TeamFlavor.NONE; // TODO
	}

}
