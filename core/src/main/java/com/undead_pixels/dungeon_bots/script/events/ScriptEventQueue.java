package com.undead_pixels.dungeon_bots.script.events;

import org.luaj.vm2.LuaDouble;
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
 * @version 2/1/2018
 */
public class ScriptEventQueue extends AbstractTaskQueue<LuaSandbox, ScriptEvent> implements Runnable {
	
	private boolean isAlive = true;
	private Thread runLoopThread;

	public ScriptEventQueue(LuaSandbox owner) {
		super(owner);
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
			runLoopThread.interrupt();
		}
	}
	
	@Override
	public void run() {
		while(isAlive) {
			this.dequeueIfIdle();
			this.act(0.0f);
			
			if(this.isEmpty()) {
				try {
					Thread.sleep(100);
				} catch(InterruptedException ex) {
					// ok; we expect periodic interrupts
				}
			}
		}
	}
	
	public void enqueueFunctionCall(String functionName, LuaValue[] args, ScriptEventStatusListener... listeners) {
		FunctionCallScriptEvent event = new FunctionCallScriptEvent(owner, functionName, args);
		
		enqueue(event, listeners);
	}
	
	public void enqueueCodeBlock(String codeBlock, ScriptEventStatusListener... listeners) {
		CodeBlockScriptEvent event = new CodeBlockScriptEvent(owner, codeBlock);
		
		enqueue(event, listeners);
	}
	
	public void enqueueFunctionCall(String functionName, LuaValue[] args, CoalescingGroup<FunctionCallScriptEvent> coalescingGroup, ScriptEventStatusListener... listeners) {
		FunctionCallScriptEvent event = new FunctionCallScriptEvent(owner, functionName, args);
		
		enqueue(event, listeners);
	}
	
	public void enqueueCodeBlock(String codeBlock, CoalescingGroup<CodeBlockScriptEvent> coalescingGroup, ScriptEventStatusListener... listeners) {
		CodeBlockScriptEvent event = new CodeBlockScriptEvent(owner, codeBlock);
		
		enqueue(event, listeners);
	}

	public void enqueue(ScriptEvent event, ScriptEventStatusListener... listeners) {
		for(ScriptEventStatusListener listener : listeners) {
			event.addListener(listener);
		}
		
		this.enqueue(event);
	}
	public <A extends ScriptEvent> void enqueue(A event, CoalescingGroup<A> coalescingGroup, ScriptEventStatusListener... listeners) {
		for(ScriptEventStatusListener listener : listeners) {
			event.addListener(listener);
		}
		
		this.enqueue(event, coalescingGroup);
	}
}
