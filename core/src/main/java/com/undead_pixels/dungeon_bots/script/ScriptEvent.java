package com.undead_pixels.dungeon_bots.script;

import org.luaj.vm2.LuaTable;

public class ScriptEvent {
	
	// This whole thing is very much TODO
	
	public static final String NO_COALESCING = "";
	
	public final String coalesceGroup;
	public final String functionCall;
	public LuaTable[] args;
	
	
	public ScriptEvent(String coalesceGroup, String functionCall, LuaTable[] args) {
		super();
		this.coalesceGroup = coalesceGroup;
		this.functionCall = functionCall;
		this.args = args;
	}
	
	

}
