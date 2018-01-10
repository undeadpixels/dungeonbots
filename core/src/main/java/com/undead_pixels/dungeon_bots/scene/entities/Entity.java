package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.script.LuaScript;

public abstract class Entity {
	protected LuaScript script;
	
	public LuaScript getScript() {
		return script;
	}
	public void setScript(LuaScript script) {
		this.script = script;
	}
	
	public Entity render() {
		return this;
	}
	public Entity update(double dt) {
		return this;
	}
	
	public abstract Vector2 getPosition();
	
	
}
