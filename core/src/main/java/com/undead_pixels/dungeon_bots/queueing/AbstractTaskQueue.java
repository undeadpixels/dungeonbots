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
	
	/**
	 * The maximum allowable number of elements in this queue.
	 * 
	 * Will throw an exception when trying to queue more.
	 */
	protected int maxSize = 1024;
	
	/**
	 * The maximum allowable number of elements to attempt to dequeue before giving up.
	 */
	protected int maxAttempts = 1024;
	

	/**
	 * Constructor
	 * 
	 * @param owner
	 */
	public AbstractTaskQueue(O owner) {
		this.owner = owner;
	}

	/**
	 * @return	The owner of this AbstractTaskQueue
	 */
	public O getOwner() {
		return owner;
	}


	/**
	 * @return	If this queue is empty or not
	 */
	public synchronized boolean isEmpty() {
		return current == null && queue.isEmpty();
	}
	
	/**
	 * @return	the size of the queue
	 */
	public synchronized int size() {
		return (current != null ? 1 : 0) + queue.size();
	}
	
	/**
	 * @return	The maximum allowable size of this queue
	 */
	public synchronized int maxSize() {
		return maxSize;
	}

	/**
	 * Adds a task to the queue
	 * 
	 * @param t	A new action
	 */
	public synchronized void enqueue(T t) {
		enqueue(t, null);
	}
	
	/**
	 * Adds a task to the queue
	 * 
	 * @param t	A new action
	 * @param group A coaelescing group
	 */
	public synchronized <A extends T> void enqueue(A t, CoalescingGroup<A> group) {
		if(group == null) {
			if(queue.size() >= maxSize) {
				// panic; the queue is too long
				throw new IndexOutOfBoundsException("Too many elements already in queue");
			} else {
				queue.add(t);
			}
		} else {
			T otherT = coalescingGroupMap.get(group);

			if(otherT == null) {
				// not yet in queue; so make maps and enqueue
				coalescingGroupMap.put(group, t);
				invCoalescingGroupMap.put(t, group);

				if(queue.size() >= maxSize) {
					// panic; the queue is too long
					throw new IndexOutOfBoundsException("Too many elements already in queue");
				} else {
					queue.add(t);
				}
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
		
	}

	/**
	 * @return	The current task. May be null.
	 */
	public synchronized T getCurrent() {
		return current;
	}

	/**
	 * @return	The current task. May be null.
	 */
	public synchronized boolean hasCurrent() {
		return current != null;
	}

	@Override
	public synchronized String toString() {
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
	public synchronized boolean dequeueIfIdle() {
		if(current != null) {
			return false;
		}
		
		int attempts = 0;
		while(!queue.isEmpty()) {
			attempts++;
			
			if(attempts >= maxAttempts) {
				return false;
			}
			
			T t = queue.removeFirst();
			CoalescingGroup<? extends T> group = invCoalescingGroupMap.remove(t);
			coalescingGroupMap.remove(group);
			
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
		T c;
		synchronized(this) {
			c = current;
			if(c == null) {
				return true;
			}
		}
		boolean isDone = c.act(dt);

		if(isDone) {
			c.postAct();
			synchronized(this) {
				current = null;
			} 
		}
		return isDone;
	}

}
