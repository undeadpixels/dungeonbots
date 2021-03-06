package com.undead_pixels.dungeon_bots.scene;

import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Player;

import com.undead_pixels.dungeon_bots.scene.entities.Actor.Direction;

public class EntityActionTest {
	
	private static void assertEquals(String message, Point2D.Float expected, Point2D.Float actual, float delta) {
		Assert.assertEquals(message, expected.x, actual.x, delta);
		Assert.assertEquals(message, expected.y, actual.y, delta);
	}


	@Test
	public void animatedMoveInSquareTest() {
		World world = new World();
		world.setSize(16, 16);
		Player p = new Player(world, "player", 1, 1);
		world.addEntity(p);

		p.queueMoveSlowly(Direction.UP, false);
		p.queueMoveSlowly(Direction.RIGHT, false);
		p.queueMoveSlowly(Direction.DOWN, false);
		p.queueMoveSlowly(Direction.LEFT, false);
		assertEquals("", new Point2D.Float(1, 1), p.getPosition(), 0.01f);

		Point2D.Float[] expected = {
				new Point2D.Float(1.0f, 1.5f),
				new Point2D.Float(1.0f, 2.0f),

				new Point2D.Float(1.5f, 2.0f),
				new Point2D.Float(2.0f, 2.0f),

				new Point2D.Float(2.0f, 1.5f),
				new Point2D.Float(2.0f, 1.0f),

				new Point2D.Float(1.5f, 1.0f),
				new Point2D.Float(1.0f, 1.0f)
		};

		int i = 0;
		for(Point2D.Float expect : expected) {
			world.update(p.getMoveDuration() / 2 + .0001f);
			//System.out.println(p.getActionQueue());
			//System.out.println(p.getPosition());
			assertEquals("Trying time step #"+(i+1), expect, p.getPosition(), 0.01f);
			i++;
		}
	}
}
