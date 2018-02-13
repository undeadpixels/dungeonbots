package com.undead_pixels.dungeon_bots.script.events;

import org.luaj.vm2.LuaValue;

import com.undead_pixels.dungeon_bots.queueing.AbstractTaskQueue;
import com.undead_pixels.dungeon_bots.queueing.CoalescingGroup;
import com.undead_pixels.dungeon_bots.script.CodeBlockScriptEvent;
import com.undead_pixels.dungeon_bots.script.FunctionCallScriptEvent;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.ScriptEvent;
import com.undead_pixels.dungeon_bots.script.ScriptEventStatusListener;

/**
 * @author Kevin Parker
 * @version 2/13/2018
 */
public class ScriptEventQueue extends AbstractTaskQueue<LuaSandbox, ScriptEvent> implements Runnable {
	
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
		this.enqueueFunctionCall("update", new LuaValue[] {LuaValue.valueOf(dt)}, UpdateCoalescer.instance);
		
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
						this.wait();
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
	 * Enqueues a lua function call
	 * 
	 * @param functionName	Name of the function to call
	 * @param args			Args to pass the function
	 * @param listeners		Things that might want to listen to the status of this event (if any)
	 * @return				The event that was enqueued
	 */
	public FunctionCallScriptEvent enqueueFunctionCall(String functionName, LuaValue[] args, ScriptEventStatusListener... listeners) {
		return enqueueFunctionCall(functionName, args, null, listeners);
	}

	/**
	 * @param codeBlock			A block of lua code to execute
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 * @return					The event that was enqueued
	 */
	public CodeBlockScriptEvent enqueueCodeBlock(String codeBlock, ScriptEventStatusListener... listeners) {
		return enqueueCodeBlock(codeBlock, null, listeners);
	}
	
	/**
	 * Enqueues a lua function call
	 * 
	 * @param functionName		Name of the function to call
	 * @param args				Args to pass the function
	 * @param coalescingGroup	A group to coalesce events into
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 * @return				The event that was enqueued
	 */
	public FunctionCallScriptEvent enqueueFunctionCall(String functionName, LuaValue[] args, CoalescingGroup<FunctionCallScriptEvent> coalescingGroup, ScriptEventStatusListener... listeners) {
		FunctionCallScriptEvent event = new FunctionCallScriptEvent(owner, functionName, args);
		
		enqueue(event, coalescingGroup, listeners);
		
		return event;
	}
	
	/**
	 * @param codeBlock			A block of lua code to execute
	 * @param coalescingGroup	A group to coalesce events into
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 * @return					The event that was enqueued
	 */
	public CodeBlockScriptEvent enqueueCodeBlock(String codeBlock, CoalescingGroup<CodeBlockScriptEvent> coalescingGroup, ScriptEventStatusListener... listeners) {
		CodeBlockScriptEvent event = new CodeBlockScriptEvent(owner, codeBlock);
		
		enqueue(event, coalescingGroup, listeners);
		
		return event;
	}
	
	/**
	 * Enqueues a ScriptEvent, attaching it to listeners
	 * 
	 * @param event				Some lua stuff to run
	 * @param coalescingGroup	A group to coalesce events into
	 * @param listeners			Things that might want to listen to the status of this event (if any)
	 */
	private <A extends ScriptEvent> void enqueue(A event, CoalescingGroup<A> coalescingGroup, ScriptEventStatusListener... listeners) {
		for(ScriptEventStatusListener listener : listeners) {
			event.addListener(listener);
		}
		
		this.enqueue(event, coalescingGroup);
	}
}
