package com.undead_pixels.dungeon_bots.scene;

import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Player;

import com.undead_pixels.dungeon_bots.scene.entities.Actor.Direction;

public class EntityActionTest {
	
	private static void assertEquals(String message, Vector2 expected, Vector2 actual, float delta) {
		Assert.assertEquals(message, expected.x, actual.x, delta);
		Assert.assertEquals(message, expected.y, actual.y, delta);
	}


    @Test
    public void animatedMoveInSqureTest() {
    		World world = new World();
    		world.setSize(16, 16);
    		Player p = new Player(world, "player");

    		p.queueMoveSlowly(Direction.UP);
    		p.queueMoveSlowly(Direction.RIGHT);
    		p.queueMoveSlowly(Direction.DOWN);
    		p.queueMoveSlowly(Direction.LEFT);
    		assertEquals("", new Vector2(0, 0), p.getPosition(), 0.01f);
    		
    		Vector2[] expected = {
    				new Vector2(.0f, .5f),
    				new Vector2(.0f, 1.f),
    				
    				new Vector2(.5f, 1.f),
    				new Vector2(1.f, 1.f),
    				
    				new Vector2(1.f, .5f),
    				new Vector2(1.f, .0f),
    				
    				new Vector2(.5f, .0f),
    				new Vector2(.0f, .0f)
    		};
    		
    		int i = 0;
    		for(Vector2 expect : expected) {
        		world.update(p.getMoveDuration() / 2 + .0001f);
        		System.out.println(p.getActionQueue());
        		System.out.println(p.getPosition());
        		assertEquals("Trying time step #"+(i+1), expect, p.getPosition(), 0.01f);
        		i++;
    		}
    }
}
