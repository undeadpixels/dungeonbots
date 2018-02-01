package com.undead_pixels.dungeon_bots.queueing;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * An abstract queue for Taskable objects.
 * 
 * For example, entities have an ActionQueue, where they enqueue actions,
 * and they're executed in order.
 * 
 * 
 * @param <O>	The type of this Queue's owner
 * @param <T>	The type of this Queue's elements
 */
public abstract class AbstractTaskQueue<O, T extends Taskable<O>> {
	/**
	 * Internal storage
	 */
	protected LinkedList<T> queue = new LinkedList<>();
	
	/**
	 * A map from a CoalescingGroup to the task it coalesces into.
	 */
	protected HashMap<CoalescingGroup<? extends T>, T> coalescingGroupMap = new HashMap<>();
	
	/**
	 * Inverse of coalescingGroupMap
	 */
	protected HashMap<T, CoalescingGroup<? extends T>> invCoalescingGroupMap = new HashMap<>();
	
	/**
	 * The current Task
	 */
	protected T current;
	
	/**
	 * The owner of this queue
	 */
	protected O owner;
	

	public AbstractTaskQueue(O owner) {
		this.owner = owner;
	}

	public O getOwner() {
		return owner;
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
	 * Adds a task to the queue
	 * 
	 * @param t	A new action
	 */
	public void enqueue(T t) {
		queue.add(t);
	}
	
	/**
	 * Adds a task to the queue
	 * 
	 * @param t	A new action
	 * @param group A coaelescing group
	 */
	public <A extends T> void enqueue(A t, CoalescingGroup<A> group) {
		T otherT = coalescingGroupMap.get(group);
		
		if(otherT == null) {
			// not yet in queue; so make maps and enqueue
			coalescingGroupMap.put(group, t);
			invCoalescingGroupMap.put(t, group);

			queue.add(t);
		} else {
			// try to coalesce now
			try {
				@SuppressWarnings("unchecked")
				A otherA = (A) otherT;
				
				group.coalesce(otherA, t);
			} catch (ClassCastException e) {
				throw new RuntimeException("Invalid coalescing group for object.");
			}
		}
		
	}

	/**
	 * @return	The current task. May be null.
	 */
	protected T getCurrent() {
		return current;
	}

	/**
	 * @return	The current task. May be null.
	 */
	public boolean hasCurrent() {
		return current != null;
	}

	@Override
	public String toString() {
		String others = "";
		
		for(T t : queue) {
			if(!others.isEmpty()) {
				others += ", ";
			}
			others += t;
		}
		
		return "["+current+" ||| " + others + "]";
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
			T t = queue.removeFirst();
			CoalescingGroup<? extends T> group = invCoalescingGroupMap.remove(t);
			coalescingGroupMap.remove(group);
			
			System.out.println("Dequeueing task "+t);
			
			boolean ok = t.preAct();
			
			if(ok) {
				current = t;
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Tells this queue to update/act.
	 * 
	 * This causes the current task in the queue to also act.
	 * 
	 * @param dt		Delta Time
	 * @return		True if this queue is done with its current event
	 */
	public boolean act(float dt) {
		T c = current;
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

}
