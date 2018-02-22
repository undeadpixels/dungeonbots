package com.undead_pixels.dungeon_bots.script;

import java.util.HashMap;

public class SandboxManager {
	
	private static HashMap<String, LuaSandbox> sandboxes = new HashMap<>();

	public static void register(Thread runLoopThread, LuaSandbox sandbox) {
		runLoopThread.setName("lua-"+System.currentTimeMillis()); // TODO - maybe get name of sandbox?
		
		sandboxes.put(runLoopThread.getName(), sandbox);
	}

	public static void delete(Thread runLoopThread) {
		sandboxes.remove(runLoopThread.getName());
	}
	
	public static LuaSandbox getCurrentSandbox() {
		String currentThreadName = Thread.currentThread().getName();
		
		return sandboxes.getOrDefault(currentThreadName, null);
	}

}
