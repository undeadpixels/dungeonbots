package com.undead_pixels.dungeon_bots.script.interfaces;

import com.undead_pixels.dungeon_bots.script.LuaBinding;

import java.util.Collection;

public interface LuaCode {
    Collection<LuaBinding> getBindings();
}
