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
	
	public static final void safeSleep(long t) {
		try { Thread.sleep(t); } catch (InterruptedException e) { }
	}
	
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
	
	@Test
	public void luaKillLockupTest() throws InterruptedException {
		String luaCode = "while true do player:right() player:left() end";
		
		boolean[] finished = {false};
		int[] updates = {0};
		
		Thread execution = new Thread(() -> {
			World w = new World();
			w.setSize(16, 16);
			w.getPlayer();
			System.out.println("Built world");
			Bot b = w.makeBot("testbot", 1, 1);
			LuaInvocation invocation = b.getSandbox().enqueueCodeBlock(luaCode);

			Thread waitingThread = new Thread(() -> {
				invocation.join(5000);
			});
			Thread wupdateThread = new Thread(() -> {
				while(! finished[0]) {
					w.update(.21f);
					updates[0]++;
				}
			});
			
			waitingThread.start();
			wupdateThread.start();
			
			System.out.println("Waiting");
			
			while(updates[0] < 10) {
				safeSleep(1);
			}

			System.out.println("killing");
			invocation.stop();

			safeSleep(100);

			Assert.assertEquals(ScriptStatus.STOPPED, invocation.getStatus());
			finished[0] = true;
			System.out.println("finished");
		});
		execution.start();
		
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime < 5000) {
			safeSleep(1);
			
			if(finished[0] == true) {
				return;
			}
		}
		
		throw new RuntimeException("Did not finish");
	}
}
