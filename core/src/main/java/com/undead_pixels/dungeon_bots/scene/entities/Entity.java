package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.BatchRenderable;
import com.undead_pixels.dungeon_bots.scene.Renderable;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public abstract class Entity implements BatchRenderable {
	protected LuaScript script;
	protected final World world;

	public Entity(World world) {
		this(world, null);
	}
	public Entity(World world, LuaScript script) {
		super();
		this.world = world;
		this.script = script;
	}
	
	public LuaScript getScript() {
		return script;
	}
	public void setScript(LuaScript script) {
		this.script = script;
	}
	
	public abstract Vector2 getPosition();
	public abstract boolean isSolid();
	
	
}
