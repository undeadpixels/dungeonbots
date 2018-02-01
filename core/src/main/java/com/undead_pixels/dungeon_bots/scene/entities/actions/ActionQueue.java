package com.undead_pixels.dungeon_bots.scene.entities.actions;

import com.undead_pixels.dungeon_bots.queueing.AbstractTaskQueue;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;

/**
 * A magical queue that keeps track of actions. Mostly just reuses AbstractTaskQueue.
 */
public class ActionQueue extends AbstractTaskQueue<Entity, Action> {
	
	/**
	 * Constructor
	 * 
	 * @param entity		The entity that this is attached to
	 */
	public ActionQueue(Entity entity) {
		super(entity);
	}
	
	@Override
	public String toString() {
		
		return "ActionQueue: "+super.toString();
	}
	
}
