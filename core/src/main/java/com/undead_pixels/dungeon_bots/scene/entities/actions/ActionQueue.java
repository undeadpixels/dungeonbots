package com.undead_pixels.dungeon_bots.scene.entities.actions;

import com.undead_pixels.dungeon_bots.queueing.AbstractTaskQueue;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;

public class ActionQueue extends AbstractTaskQueue<Entity, Action> {
	
	public ActionQueue(Entity entity) {
		super(entity);
	}
	
	public String toString() {
		
		return "ActionQueue: "+super.toString();
	}
	
}
