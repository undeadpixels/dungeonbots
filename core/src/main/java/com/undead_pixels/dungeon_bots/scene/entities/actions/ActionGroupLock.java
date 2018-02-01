package com.undead_pixels.dungeon_bots.scene.entities.actions;

import java.util.LinkedList;

/**
 * A class indicating that actions for a particular group should be locked until completed
 */
public class ActionGroupLock {
	
	/**
	 * The queues to watch
	 */
	private LinkedList<ActionQueue> busyQueues = new LinkedList<>();
	
	/**
	 * @param queue	The queue to group into this lock (and wait for before releasing this lock)
	 */
	public void add(ActionQueue queue) {
		busyQueues.add(queue);
	}
	
	/**
	 * @return	true if all queues this is watching have finished
	 */
	public boolean isFinished() {
		while(! busyQueues.isEmpty()) {
			ActionQueue aq = busyQueues.peekFirst();
			
			if(! aq.hasCurrent()) {
				busyQueues.removeFirst();
			} else {
				// The queue at the top of our list is still busy.
				// Return that we aren't done yet.
				return false;
			}
		}
		
		return true;
	}
	
}
