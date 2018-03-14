package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Bot;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;

public class ActorBuilder {
	private World world = new World();
	private String name = "actor";

	public ActorBuilder setWorld(World world) {
		this.world = world;
		return this;
	}

	public ActorBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public Actor createActor() {
		Actor ret = new Bot(world, name); // TODO - what to do with script?
		world.addEntity(ret);
		return ret;
	}
}