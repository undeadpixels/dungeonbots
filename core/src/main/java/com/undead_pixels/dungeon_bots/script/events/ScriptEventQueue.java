package com.undead_pixels.dungeon_bots.script.events;

import com.undead_pixels.dungeon_bots.queueing.AbstractTaskQueue;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.ScriptEvent;

/**
 * @author Kevin Parker
 * @version 2/1/2018
 */
public class ScriptEventQueue extends AbstractTaskQueue<LuaSandbox, ScriptEvent> {

	public ScriptEventQueue(LuaSandbox owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}
}
