package com.undead_pixels.dungeon_bots.script;

import org.luaj.vm2.LuaTable;

public class FunctionCallScriptEvent extends ScriptEvent {
	
	public final String functionCall;
	public LuaTable[] args;
	
	
	public FunctionCallScriptEvent(LuaSandbox sandbox, String functionCall, LuaTable[] args) {
		super(sandbox);
		this.functionCall = functionCall;
		this.args = args;
	}


	@Override
	public boolean startScript() {
		// TODO Auto-generated method stub
		return false;
	}

}
