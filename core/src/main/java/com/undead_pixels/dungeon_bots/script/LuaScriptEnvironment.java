package com.undead_pixels.dungeon_bots.script;
import com.undead_pixels.dungeon_bots.script.interfaces.LuaCode;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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

    public LuaScriptEnvironment add(Collection<LuaBinding> bindings) {
        bindings.forEach(binding -> globals.set(binding.bindTo, binding.luaValue));
        return this;
    }

    public LuaScriptEnvironment add(LuaBinding... bindings) {
        return add(Stream.of(bindings).collect(Collectors.toList()));
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
