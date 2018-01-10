package com.undead_pixels.dungeon_bots.script;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

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

    public LuaScriptEnvironment(Globals globals) {
        this.globals = globals;
    }

    public LuaScript scriptFromFile(File file) {
        throw new RuntimeException("Not Implemented");
    }

    public LuaScript scriptFromString(String script) {
        return new LuaScript(this, script);
    }

    public Globals getGlobals() {
        return globals;
    }

    public void setGlobals(Globals globals) {
        this.globals = globals;
    }
}
