package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.utils.annotations.*;

/**
 * Pretty much everything visible/usable within a regular game. Does not include UI elements.
 */
public abstract class Entity extends Scriptable implements BatchRenderable {

	/**
	 * A user script that is run on this object
	 */
	protected LuaScript script;
	
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
	 * @param script		A user script that is run on this object
	 */
	public Entity(World world, String name, LuaScript script) {
		super();
		this.world = world;
		this.script = script;
		this.name = name;
		this.id = world.makeID();
		setWhitelist(this.defaultWhitelist());
	}

	public Entity(World world, String name, LuaScript luaScript, Whitelist whitelist) {
		this(world,name,luaScript);
		this.whitelist = whitelist;
	}

	/**
	 * @return		The user script
	 */
	public LuaScript getScript() {
		return script;
	}
	
	/**
	 * @param script		The user script to set
	 */
	public void setScript(LuaScript script) {
		this.script = script;
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
	 * Generates a LuaScriptEnvironment for the given entity
	 * @param securityLevel The Security level of the requested LuaScriptEnvironment
	 * @return
	 */
	public LuaScriptEnvironment getScriptEnvironment(SecurityLevel securityLevel) {
	    LuaScriptEnvironment scriptEnvironment = new LuaScriptEnvironment(securityLevel);
	    scriptEnvironment.add(getBindings(this.name, securityLevel));
	    return scriptEnvironment;
    }

	/**
	 * Generates a LuaScriptEnvironment for the given entity
	 * @return
	 */
	public LuaScriptEnvironment getScriptEnvironment() {
		return getScriptEnvironment(SecurityLevel.AUTHOR);
	}
}
