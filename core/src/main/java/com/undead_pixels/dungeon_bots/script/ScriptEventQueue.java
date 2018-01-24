package com.undead_pixels.dungeon_bots.script;

import java.util.LinkedList;

public class ScriptEventQueue {
	
	private LinkedList<ScriptEvent> fixme = new LinkedList<>();
	
	public void push(ScriptEvent ev) {
		fixme.push(ev);
	}
	
	public ScriptEvent pop() {
		return fixme.pop();
	}

}
