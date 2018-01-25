package com.undead_pixels.dungeon_bots.scene;

import java.util.LinkedList;

import com.undead_pixels.dungeon_bots.scene.entities.ActionQueue;

/**
 * A class indicating that actions for a particular group should be locked until eveLinkedList<E> completed
 */
public class ActionGroupLock {
	
	private LinkedList<ActionQueue> busyQueues = new LinkedList<>();
	
	public void add(ActionQueue queue) {
		busyQueues.add(queue);
	}
	
	public boolean isFinished() {
		while(! busyQueues.isEmpty()) {
			ActionQueue aq = busyQueues.peekFirst();
			
			if(aq.getCurrent() == null) {
				busyQueues.removeFirst();
			} else {
				return false;
			}
		}
		
		return true;
	}
	
}
