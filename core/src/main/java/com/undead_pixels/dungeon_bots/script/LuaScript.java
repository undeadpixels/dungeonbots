package com.undead_pixels.dungeon_bots.script;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.File;
import java.util.Optional;

public class LuaScript {
    private Thread thread;
    private final LuaScriptEnvironment environment;
    private final String script;
    private volatile Varargs varargs;

    public LuaScript(LuaScriptEnvironment env, String script) {
        this.environment = env;
        this.script = script;
    }

    public LuaScript toFile(File f) {
        throw new RuntimeException("Not Implemented");
    }

    public synchronized LuaScript start() {
        thread = new Thread(() -> {
            LuaValue chunk = environment.getGlobals().load(this.script);
            varargs = chunk.invoke();
        });
        thread.start();
        return this;
    }
    
    public synchronized LuaScript stop() {
        throw new RuntimeException("Not Implemented");
    }

    public synchronized ScriptStatus getStatus() {
        throw new RuntimeException("Not Implemented");
    }

    public synchronized LuaScript resume() {
        throw new RuntimeException("Not Implemented");
    }

    public synchronized LuaScript pause() {
        throw new RuntimeException("Not Implemented");
    }

    public synchronized Optional<LuaScript> join() {
        return join(0);
    }

    public synchronized Optional<LuaScript> join(long wait) {
        try {
            thread.getState();
            thread.join(wait);
            return thread.isAlive() ? Optional.empty() : Optional.of(this);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    public synchronized Optional<Varargs> getResults() {
        return Optional.ofNullable(varargs);
    }
}
