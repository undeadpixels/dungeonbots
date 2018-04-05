/**
 * 
 */
package com.undead_pixels.dungeon_bots.scene;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.scene.entities.Bot;
import com.undead_pixels.dungeon_bots.scene.entities.Door;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.script.LuaInvocation;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;

/**
 * @author kevin
 *
 */
public class EventsTest {
	
	private static class OutputListener implements Supplier<Boolean>, Consumer<String> {
		
		HashSet<String> lookingFor;
		
		/**
		 * @param string
		 */
		public OutputListener(String... strings) {
			lookingFor = new HashSet<String>();
			for(String s : strings) {
				lookingFor.add(s);
			}
		}

		/* (non-Javadoc)
		 * @see java.util.function.Supplier#get()
		 */
		@Override
		public Boolean get () {
			return lookingFor.isEmpty();
		}

		/* (non-Javadoc)
		 * @see java.util.function.Consumer#accept(java.lang.Object)
		 */
		@Override
		public void accept (String t) {
			boolean matched = false;
			HashSet<String> rem = new HashSet<>();
			for(String s : lookingFor) {
				if(t.contains(s)) {
					matched = true;
					rem.add(s);
				}
			}
			
			for(String s : rem) {
				lookingFor.remove(s);
			}
			
			if(matched) {
				System.out.println("Good output: "+t);
			} else {
				System.out.println("Un-helpful output: "+t);
				System.out.println(" > Not in: "+lookingFor);
			}
		}
		
	}

	private void checkForOutput(LuaSandbox sand, Runnable act, String... str) {
		checkForOutput(sand, act, 1000, str);
	}
	private void checkForOutput(LuaSandbox sand, Runnable act, int timeout, String... str) {
		OutputListener ol = new OutputListener(str);
		sand.addOutputEventListener(ol);
		
		Thread thr = new Thread(act);
		thr.start();
		
		try {
			long begin = System.currentTimeMillis();
			while(ol.get() == false) {
				if(timeout > 0 && System.currentTimeMillis() - begin > timeout) {
					throw new InterruptedException("Timeout of "+timeout+" expired");
				}
				Thread.sleep(1);
			}
		} catch(InterruptedException ex) {
			thr.interrupt();
			Assert.fail("Missing output: "+ol.lookingFor);
		}

		thr.interrupt();
	}

	void updateSome(World w) {
		for(int i = 0; i < 100; i++) {
			System.out.println("Updated");
			w.update(1);
			
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	@Test
	public void testUpdateEvent() {
		World w = new World();
		w.getLevelScripts().add(new UserScript("init",
				"print(\"init\")\n" +
				"registerUpdateListener(function(dt)\n" + 
				"  print(\"updated\")\n" + 
				"end)"));

		w.runInitScripts();
		checkForOutput(w.getSandbox(), () -> w.update(1), "updated");
		
	}

	@Test
	public void testDoorEvents() {
		World w = new World();

		Bot b = new Bot(w, "b", 1, 1);
		Door d = new Door(w, 2, 1);

		w.addEntity(d); // TODO - seems to have a race condition where the bot can try useRight before door can finish registering for events
		w.addEntity(b);
		
		b.getInventory().addItem(new Key(w));
		
		b.getScripts().add(new UserScript("init",
				"sleep(.001) " +
				"bot:useRight() " +
				"bot:inventory():peek(1):useRight() " +
				"bot:right():right() " +
				"print(\"not done?\") " +
				"bot:useLeft() " +
				"print(\"done?\")"));

		d.getScripts().add(new UserScript("init",
				"registerOpenListener(function(dt)  print(\"opened\")  end) " +
				"registerCloseListener(function(dt)  print(\"closed\")  end) " +
				"registerEnterListener(function(dt)  print(\"entered\")  end) " +
				"registerLockListener(function(dt)  print(\"locked\")  end) " +
				"registerUnlockListener(function(dt)  print(\"unlocked\")  end) " +
				"print(\"Registered callbacks\")"));

		w.runInitScripts();
		checkForOutput(d.getSandbox(), () -> updateSome(w), "opened", "entered", "closed", "unlocked", "locked");
		
	}
	


	@Test
	public void testSignEvents() {
		World w = new World();

		Bot b = new Bot(w, "b", 1, 1);
		Door d = new Door(w, 2, 1);

		w.addEntity(d); // TODO - seems to have a race condition where the bot can try useRight before door can finish registering for events
		w.addEntity(b);
		
		b.getInventory().addItem(new Key(w));
		
		b.getScripts().add(new UserScript("init",
				"sleep(.001) " +
				"bot:useRight() " +
				"bot:inventory():peek(1):useRight() " +
				"bot:right():right() " +
				"print(\"not done?\") " +
				"bot:useLeft() " +
				"print(\"done?\")"));

		d.getScripts().add(new UserScript("init",
				"registerOpenListener(function(dt)  print(\"opened\")  end) " +
				"registerCloseListener(function(dt)  print(\"closed\")  end) " +
				"registerEnterListener(function(dt)  print(\"entered\")  end) " +
				"registerLockListener(function(dt)  print(\"locked\")  end) " +
				"registerUnlockListener(function(dt)  print(\"unlocked\")  end) " +
				"print(\"Registered callbacks\")"));

		w.runInitScripts();
		checkForOutput(d.getSandbox(), () -> updateSome(w), "opened", "entered", "closed", "unlocked", "locked");
		
	}
}
