package com.undead_pixels.dungeon_bots.script;

import java.util.HashMap;

public class SandboxManager {
	
	private static HashMap<ThreadGroup, LuaSandbox> sandboxes = new HashMap<>();

	public static void register(Thread runLoopThread, LuaSandbox sandbox) {
		String name = (sandbox.toString() + " Thread").replace(" ", "");
		runLoopThread.setName(name);
		
		sandboxes.put(sandbox.getThreadGroup(), sandbox);
	}

	public static void delete(Thread runLoopThread) {
		sandboxes.remove(runLoopThread.getThreadGroup());
	}
	
	public static LuaSandbox getCurrentSandbox() {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		
		while(group != null) {
			LuaSandbox ret = sandboxes.getOrDefault(group, null);
			
			if(ret != null) {
				return ret;
			}
			
			group = group.getParent();
		}
		
		return null;
	}

}
