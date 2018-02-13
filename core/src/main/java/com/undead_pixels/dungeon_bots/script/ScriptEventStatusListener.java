package com.undead_pixels.dungeon_bots.script;

public interface ScriptEventStatusListener {
	public void scriptEventFinished(ScriptEvent event, ScriptStatus status);
	public void scriptEventStarted(ScriptEvent event, ScriptStatus status);
}
