package com.undead_pixels.dungeon_bots.lua;

import com.undead_pixels.dungeon_bots.utils.annotations.ScriptAPI;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Java Class that represents a single argument Lua Function
 */
public class LuaTestMethod extends OneArgFunction {
    @Override
    public LuaValue call(LuaValue arg) {
        return CoerceJavaToLua.coerce(arg.toint() - 1);
    }
}