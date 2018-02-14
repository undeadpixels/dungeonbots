package com.undead_pixels.dungeon_bots.script;

public interface ScriptEventStatusListener {
	public void scriptEventFinished(LuaInvocation script, ScriptStatus status);
	public void scriptEventStarted(LuaInvocation script, ScriptStatus status);
}
