package com.undead_pixels.dungeon_bots.script;

import org.luaj.vm2.LuaValue;

public class LuaBinding {
    public final String bindTo;
    public final LuaValue luaValue;

    public LuaBinding(String bindTo, LuaValue luaValue) {
        this.bindTo = bindTo;
        this.luaValue = luaValue;
    }
}
