package com.undead_pixels.dungeon_bots.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Java class that represents a 0 argument Lua Function
 */
public class LuaTestClass extends ZeroArgFunction {
    public int number;

    @Override
    public LuaValue call() {
        number = 100;
        return CoerceJavaToLua.coerce(number);
    }
}
