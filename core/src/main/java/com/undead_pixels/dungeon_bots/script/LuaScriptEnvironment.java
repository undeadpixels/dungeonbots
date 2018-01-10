package com.undead_pixels.dungeon_bots.script;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.util.stream.Stream;

public class LuaScriptEnvironment {

    private Globals globals;

    public LuaScriptEnvironment() {
        globals = JsePlatform.standardGlobals();
    }

    public LuaScriptEnvironment(LuaValue... args) {
        globals = new Globals();
        Stream.of(args).forEach(val -> globals.load(val));
    }

    public LuaScript fromFile(File file) {
        throw new NotImplementedException();
    }

    public LuaScript fromString(String script) {
        throw new NotImplementedException();
    }
}
