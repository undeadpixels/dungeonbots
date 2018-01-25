package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.interfaces.LuaReflection;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;

/**
 * Pretty much everything visible/usable within a regular game. Does not include UI elements.
 */
public abstract class Entity implements BatchRenderable, Scriptable, LuaReflection {

	/**
	 * A user scriptEnv that is run on this object
	 */
	protected LuaSandbox scriptEnv;
	
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
		world.addEntity(this);
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param scriptEnv		A user scriptEnv that is run on this object
	 */
	public Entity(World world, String name, LuaSandbox scriptEnv) {
		super();
		this.world = world;
		this.scriptEnv = scriptEnv;
		this.name = name;
		this.id = world.makeID();
	}

	public Entity(World world, String name, LuaSandbox luaSandbox, Whitelist whitelist) {
		this(world,name, luaSandbox);
	}

	/**
	 * @return		The user scriptEnv
	 */
	public LuaSandbox getScriptEnv() {
		return scriptEnv;
	}
	
	/**
	 * @param scriptEnv		The user scriptEnv to set
	 */
	public void setScriptEnv(LuaSandbox scriptEnv) {
		this.scriptEnv = scriptEnv;
	}
	
	/**
	 * @return		This Entity's position in tile space
	 */
	public abstract Vector2 getPosition();
	
	
	/**
	 * @return		If this object disallows movement through it
	 */
	public abstract boolean isSolid();

}
