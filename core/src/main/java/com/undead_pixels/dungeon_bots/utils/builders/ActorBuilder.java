package com.undead_pixels.dungeon_bots.utils.builders;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.Whitelist;

public class ActorBuilder {
	private World world = new World();
	private String name = "actor";
	private TextureRegion tex = null;
	private LuaSandbox script = null;

	public ActorBuilder setWorld(World world) {
		this.world = world;
		return this;
	}

	public ActorBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public ActorBuilder setTex(TextureRegion tex) {
		this.tex = tex;
		return this;
	}

	public ActorBuilder setScript(LuaSandbox script) {
		this.script = script;
		return this;
	}

	public Actor createActor() {
		return new Actor(world, name, script, tex);
	}
}