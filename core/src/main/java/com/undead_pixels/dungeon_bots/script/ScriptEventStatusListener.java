package com.undead_pixels.dungeon_bots.script;

public interface ScriptEventStatusListener {
	void scriptEventFinished(LuaInvocation script, ScriptStatus status);
	void scriptEventStarted(LuaInvocation script, ScriptStatus status);
}
