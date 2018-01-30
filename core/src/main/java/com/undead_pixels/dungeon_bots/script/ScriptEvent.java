package com.undead_pixels.dungeon_bots.script;

import java.util.ArrayList;

import org.luaj.vm2.LuaTable;

import com.undead_pixels.dungeon_bots.queueing.Taskable;

public abstract class ScriptEvent implements Taskable<LuaSandbox> {
	
	LuaSandbox sandbox;
	LuaScript script;
	
	ArrayList<ScriptEventStatusListener> listeners = new ArrayList<>();
	
	public void addListener(ScriptEventStatusListener listener) {
		listeners.add(listener);
	}
	
	public ScriptEvent(LuaSandbox sandbox) {
		this.sandbox = sandbox;
	}
	
	/**
	 * Create the given script and begin executing it
	 * 
	 * @return	true if the script will run as planned; false if it cannot execute
	 */
	@Override
	public boolean preAct() {
		boolean couldStart = startScript();
		
		if(!couldStart) {
			for(ScriptEventStatusListener l: listeners) {
				l.scriptEventFinished(this, ScriptStatus.ERROR);
			}
			
			return false;
		}
		
		return true;
	}


	@Override
	public boolean act(float dt) {
		if(script.getStatus() == ScriptStatus.COMPLETE
				|| script.getStatus() == ScriptStatus.ERROR
				|| script.getStatus() == ScriptStatus.LUA_ERROR) { // TODO @stewart, did I forget any cases here?
			return true;
		} else {
			// TODO - script.resume();
		}
		return false;
	}
	
	@Override
	public void postAct() {
		for(ScriptEventStatusListener l: listeners) {
			l.scriptEventFinished(this, script.getStatus());
		}
	}
	
	public abstract boolean startScript();

}
