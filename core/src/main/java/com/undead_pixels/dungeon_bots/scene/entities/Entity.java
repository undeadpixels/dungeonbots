package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.scene.entities.actions.ActionQueue;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.UseItem;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import com.undead_pixels.dungeon_bots.script.interfaces.HasEntity;
import com.undead_pixels.dungeon_bots.script.interfaces.HasTeam;

/**
 * @author Kevin Parker
 * @version 1.0 Pretty much everything visible/usable within a regular game.
 *          Does not include UI elements.
 */
@SuppressWarnings("serial")
public abstract class Entity implements BatchRenderable, GetLuaSandbox, GetLuaFacade, Serializable, UseItem, HasEntity, HasTeam {

	/**
	 * The script associated with this entity. Access to this collection is
	 * thread-safe and synchronized (though I've never actually concurrency-
	 * tested the Collections.synchronizedList() function).
	 */
	public List<UserScript> eventScripts = Collections.synchronizedList(new ArrayList<UserScript>());

	/**
	 * A user sandbox that is run on this object
	 */
	protected transient LuaSandbox sandbox = new LuaSandbox(SecurityLevel.DEFAULT);

	// TODO: WO - a separate sandbox for players' events to run in?
	protected transient LuaSandbox player_accessible_sandbox = new LuaSandbox(SecurityLevel.DEFAULT);

	/**
	 * A string representing this Entity's script (if any)
	 */
	protected String scriptText;

	/**
	 * The queue of actions this Entity is going to take
	 */
	protected transient ActionQueue actionQueue = new ActionQueue(this);

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
	 * Constructor for this entity
	 * 
	 * @param world
	 *            The world
	 * @param name
	 *            This entity's name
	 */
	public Entity(World world, String name) {
		this.world = world;
		this.name = name;
		this.id = world.makeID();
		this.sandbox.addBindable(this);
		this.eventScripts.add(new UserScript("onMyTurn", "--Do nothing", SecurityLevel.DEFAULT));
	}

	/**
	 * Returns the Lua sandbox wherein this entity's scripts will execute. WO: a
	 * distinct sandbox implemented for the player's scripts?
	 */
	@Override
	public LuaSandbox getSandbox() {
		return sandbox;
	}

	/** Called during the game loop to update the entity's status. */
	@Override
	public void update(float dt) {
		// TODO - sandbox.resume();
		actionQueue.act(dt);
		sandbox.update(dt);
	}

	/**
	 * @param sandbox
	 *            The user sandbox to set
	 */
	public void setSandbox(LuaSandbox sandbox) {
		this.sandbox = sandbox;
	}

	/**
	 * @param vals
	 *            The values to add to the sandbox
	 * @return this
	 */
	@SafeVarargs
	public final <T extends GetLuaFacade> Entity addToSandbox(T... vals) {
		this.sandbox.addBindable(vals);
		return this;
	}

	/**
	 * @return This Entity's position in tile space
	 */
	public abstract Point2D.Float getPosition();

	/**
	 * Derived classes must implement.
	 *
	 * @return If this object disallows movement through it.
	 */
	public abstract boolean isSolid();

	/**
	 * TODO - should this be private?
	 * 
	 * @return This Entity's action queue
	 */
	public ActionQueue getActionQueue() {
		return actionQueue;
	}

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.NONE; // TODO - store info on the actual team, maybe (or just have overrides do this right)
	}
	
	@Override
	public Entity getEntity() {
		return this;
	}

	/**
	 * @return The world this entity belongs to
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Returns an ID number associated with this entity. The ID number should
	 * not be user-facing.
	 */
	public final int getId() {
		return this.id;
	}

	/** Returns the name of this entity. */
	public final String getName() {
		return this.name;
	}

	// WO: is this used by anything? I think view scaling was implemented at
	// world level.
	@Deprecated
	public abstract float getScale();

	// WO: a part of serialization?
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		sandbox = new LuaSandbox(SecurityLevel.DEFAULT);
		actionQueue = new ActionQueue(this);
	}
}
