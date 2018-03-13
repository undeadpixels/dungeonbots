package com.undead_pixels.dungeon_bots.scene;
import com.undead_pixels.dungeon_bots.utils.builders.ActorBuilder;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Bot;
import com.undead_pixels.dungeon_bots.script.LuaInvocation;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;

import org.junit.Assert;

public class WorldTest {
	
	@Test
	public void simpleTest() {
		World w = new World();
		Actor p = new ActorBuilder().setWorld(w).setName("test actor").setTex(null).createActor();
		
		Assert.assertEquals("Actor x at beginning", 0.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at beginning", 0.0f, p.getPosition().y, .0001);
		p.moveInstantly(Actor.Direction.RIGHT, 5);
		Assert.assertEquals("Actor x at step 1", 5.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at step 1", 0.0f, p.getPosition().y, .0001);
		p.moveInstantly(Actor.Direction.UP, 5);
		Assert.assertEquals("Actor x at step 2", 5.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at step 2", 5.0f, p.getPosition().y, .0001);
		p.moveInstantly(Actor.Direction.LEFT, 5);
		Assert.assertEquals("Actor x at step 3", 0.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at step 3", 5.0f, p.getPosition().y, .0001);
		p.moveInstantly(Actor.Direction.DOWN, 5);
		Assert.assertEquals("Actor x at end", 0.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at end", 0.0f, p.getPosition().y, .0001);
	}

}
