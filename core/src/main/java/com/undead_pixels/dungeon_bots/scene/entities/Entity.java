package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.BatchRenderable;
import com.undead_pixels.dungeon_bots.scene.Renderable;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaScript;

/**
 * Pretty much everything visible/usable within a regular game. Does not include UI elements.
 */
public abstract class Entity implements BatchRenderable {
	/**
	 * A user script that is run on this object
	 */
	protected LuaScript script;
	
	/**
	 * The world of which this Entity is a part
	 */
	protected final World world;

	/**
	 * @param world		The world to contain this Actor
	 */
	public Entity(World world) {
		this(world, null);
		world.addEntity(this);
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user script that is run on this object
	 */
	public Entity(World world, LuaScript script) {
		super();
		this.world = world;
		this.script = script;
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
	 * @return		This Entity's position in screen space
	 */
	public abstract Vector2 getPosition();
	
	
	/**
	 * @return		If this object disallows movement through it
	 */
	public abstract boolean isSolid();
	
	
}
