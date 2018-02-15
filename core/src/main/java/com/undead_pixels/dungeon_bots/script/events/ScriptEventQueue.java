package com.undead_pixels.dungeon_bots.script.events;

import com.undead_pixels.dungeon_bots.queueing.*;
import com.undead_pixels.dungeon_bots.script.*;

/**
 * @author Kevin Parker
 * @version 2/13/2018
 */
public class ScriptEventQueue extends AbstractTaskQueue<LuaSandbox, LuaInvocation> implements Runnable {
	
	/**
	 * A flag tracking if this has been killed
	 */
	private boolean isAlive = true;
	
	/**
	 * Internal thread that consumes events
	 */
	private Thread runLoopThread;

	public ScriptEventQueue(LuaSandbox owner) {
		super(owner);
	}
	
	/**
	 * Permanently murders this ScriptEventQueue
	 */
	public void kill() {
		isAlive = false;
	}

	/**
	 * Calls the update() lua function and wakes up this queue
	 * 
	 * @param dt
	 */
	public void update(float dt) {
		//owner.enqueueFunctionCall("update", new LuaValue[] {LuaValue.valueOf(dt)}, UpdateCoalescer.instance);

		if(runLoopThread == null) {
			runLoopThread = new Thread(this);
			runLoopThread.start();
		} else {
			synchronized(this) {
				this.notify();
			}
		}
	}
	
	@Override
	public void run() {
		while(isAlive) {
			this.dequeueIfIdle();
			this.act(0.0f);
			
			if(this.isEmpty()) {
				synchronized(this) {
					try {
						this.wait(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
	@Override
	public boolean act(float dt) {
		// only allow this SEQ to be run from the runLoopThread.
		if(Thread.currentThread() == runLoopThread) {
			return super.act(dt);
		} else {
			return false;
		}
	}
	
	/**
	 * Enqueues a ScriptEvent, attaching it to listeners
	 * 
	 * @param event				Some lua stuff to run
	 * @param coalescingGroup	A group to coalesce events into
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 */
	public <A extends LuaInvocation> void enqueue(A event, CoalescingGroup<A> coalescingGroup, ScriptEventStatusListener... listeners) {
		for(ScriptEventStatusListener listener : listeners) {
			event.addListener(listener);
		}
		//System.out.println("Enqueueing "+event);
		this.enqueue(event, coalescingGroup);
	}
}
