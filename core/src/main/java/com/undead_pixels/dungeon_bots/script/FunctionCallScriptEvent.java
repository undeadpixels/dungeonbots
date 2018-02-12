package com.undead_pixels.dungeon_bots.script;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class FunctionCallScriptEvent extends ScriptEvent {
	
	public final String functionCall;
	public LuaValue[] args;
	
	
	public FunctionCallScriptEvent(LuaSandbox sandbox, String functionCall, LuaValue[] luaValues) {
		super(sandbox);
		this.functionCall = functionCall;
		this.args = luaValues;
	}


	@Override
	public boolean startScript() {
		// TODO Auto-generated method stub
		return false;
	}

}
