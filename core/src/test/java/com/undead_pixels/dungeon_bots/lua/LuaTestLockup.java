/**
 * 
 */
package com.undead_pixels.dungeon_bots.lua;

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Bot;
import com.undead_pixels.dungeon_bots.script.LuaInvocation;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;

/**
 * @author kevin
 *
 */
public class LuaTestLockup {
	
	private static final void sleep(long t) {
		try { Thread.sleep(t); } catch (InterruptedException e) { }
	}

	private static final boolean sleepyWait(Supplier<Boolean> trigger) {
		return sleepyWait(15000, trigger);
	}
	private static final boolean sleepyWait(long timeout, Supplier<Boolean> trigger) {
		long startTime = System.currentTimeMillis();
		
		while(trigger.get() == false) {
			if(System.currentTimeMillis() - startTime > timeout) {
				return false;
			}
			sleep(1);
		}
		
		return true;
	}
	
	public void runLockup(String script, long timeout, int warmup) {
		
		boolean[] finished = {false};
		boolean[] assertionsPassed = {false};
		boolean[] joinFinished = {false};
		boolean[] isRunning = {false};
		int[] updates = {0};
		
		Thread execution = new Thread(() -> {
			World w = new World();
			w.setSize(16, 16);
			System.out.println("Built world");
			Bot b = w.makeBot("bot", 1, 1);
			LuaInvocation invocation = b.getSandbox().enqueueCodeBlock(script);

			Thread waitingThread = new Thread(() -> {
				invocation.join(timeout);
				joinFinished[0] = true;
			});
			Thread wupdateThread = new Thread(() -> {
				while(! finished[0] && isRunning[0]) {
					w.update(.21f);
					updates[0]++;
				}
			});
			
			waitingThread.start();
			wupdateThread.start();
			
			System.out.println("Waiting");
			
			sleepyWait(() -> updates[0] < warmup);

			System.out.println("killing");
			invocation.stop();
			System.out.println("done killing");
			finished[0] = true;

			Assert.assertNotNull(invocation.getError());
			if(!invocation.getError().getMessage().contains("ScriptInterruptException")) {
				throw new RuntimeException("Error does not contain ScriptInterruptException;\ninstead = " + invocation.getError().getMessage());
			}
			Assert.assertEquals(ScriptStatus.STOPPED, invocation.getStatus());
			
			System.out.println("finished");
			assertionsPassed[0] = true;
		});
		execution.start();

		if(! sleepyWait(timeout, () -> finished[0] == true)) {
			isRunning[0] = false;
			throw new RuntimeException("Did not finish");
		}

		if(! sleepyWait(timeout, () -> joinFinished[0] == true)) {
			isRunning[0] = false;
			throw new RuntimeException("Join did not finish");
		}

		if(! sleepyWait(timeout, () -> assertionsPassed[0] == true)) {
			isRunning[0] = false;
			throw new RuntimeException("Assertions did not pass");
		}
		
		isRunning[0] = false;
	}

	@Test
	public void luaMoveKillLockupTest() throws InterruptedException {
		runLockup("while true do\n"
				+ "bot:right()\n"
				+ "bot:left()\n"
				+ "end", 3000, 5);
	}

	@Test
	public void luaSleepKillLockupTest() throws InterruptedException {
		runLockup("sleep(10)", 1000, 5);
	}
}
