package com.undead_pixels.dungeon_bots.scene;
import com.undead_pixels.dungeon_bots.utils.builders.ActorBuilder;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;

import org.junit.Assert;

public class WorldTest {

    @Test
	public void simpleTest() {
		World w = new World();
		Actor p = new ActorBuilder().setWorld(w).setName("test actor").setTex(null).createActor();

		Assert.assertEquals("Actor x at beginning", 0.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at beginning", 0.0f, p.getPosition().y, .0001);
		p.moveInstantly(Actor.Direction.RIGHT, 5);
		Assert.assertEquals("Actor x at middle", 5.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at middle", 0.0f, p.getPosition().y, .0001);
		p.moveInstantly(Actor.Direction.UP, 5);
		Assert.assertEquals("Actor x at middle", 5.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at middle", 5.0f, p.getPosition().y, .0001);
		p.moveInstantly(Actor.Direction.LEFT, 5);
		Assert.assertEquals("Actor x at middle", 0.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at middle", 5.0f, p.getPosition().y, .0001);
		p.moveInstantly(Actor.Direction.DOWN, 5);
		Assert.assertEquals("Actor x at end", 0.0f, p.getPosition().x, .0001);
		Assert.assertEquals("Actor y at end", 0.0f, p.getPosition().y, .0001);
		
		
	}
}
