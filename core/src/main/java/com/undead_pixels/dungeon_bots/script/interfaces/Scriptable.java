package com.undead_pixels.dungeon_bots.script.interfaces;
import org.luaj.vm2.LuaValue;

public interface Scriptable {
	int getId();
	String getName();
	default LuaValue getLuaBinding() {
		return LuaValue.NIL;
	}
}
