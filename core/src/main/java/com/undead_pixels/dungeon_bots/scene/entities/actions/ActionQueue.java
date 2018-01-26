package com.undead_pixels.dungeon_bots.scene.entities.actions;

import java.util.LinkedList;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;

public class ActionQueue {
	private LinkedList<Action> queue = new LinkedList<>();
	private Action current;
	private Entity entity;
	
	public ActionQueue(Entity entity) {
		super();
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	/**
	 * @return	If this queue is empty or not
	 */
	public boolean isEmpty() {
		return current == null && queue.isEmpty();
	}
	
	/**
	 * @return	the size of the queue
	 */
	public int size() {
		return (current != null ? 1 : 0) + queue.size();
	}
	
	/**
	 * Adds an action to the queue
	 * 
	 * @param a	A new action
	 */
	public void enqueue(Action a) {
		queue.add(a);
	}

	/**
	 * @return	The current action. May be null.
	 */
	private Action getCurrent() {
		return current;
	}

	/**
	 * @return	The current action. May be null.
	 */
	public boolean hasCurrent() {
		return current != null;
	}
	
	/**
	 * If current is null and there are remaining elements in the queue,
	 * pops until a valid one is found and sets current to that. 
	 */
	public boolean dequeueIfIdle() {
		if(current != null) {
			return false;
		}
		
		while(!queue.isEmpty()) {
			Action a = queue.removeFirst();
			
			System.out.println("Dequeueing action "+a);
			
			boolean ok = a.preAct();
			
			if(ok) {
				current = a;
				return true;
			}
		}
		
		return false;
	}
	
	public boolean act(float dt) {
		Action c = current;
		if(c == null) {
			return true;
		}
		boolean isDone = c.act(dt);
		
		if(isDone) {
			c.postAct();
			current = null;
		}
		return isDone;
	}
	
	public String toString() {
		String others = "";
		
		for(Action a : queue) {
			if(!others.isEmpty()) {
				others += ", ";
			}
			others += a;
		}
		
		return "ActionQueue: ["+current+" ||| " + others + "]";
	}
	
}
