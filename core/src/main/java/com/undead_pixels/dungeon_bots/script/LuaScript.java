package com.undead_pixels.dungeon_bots.script;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.File;
import java.util.Optional;

public class LuaScript {

    private final LuaScriptEnvironment environment;
    private final String script;
    private volatile Varargs varargs;
    private volatile ScriptStatus scriptStatus;
    private Thread thread;

    public LuaScript(LuaScriptEnvironment env, String script) {
        this.environment = env;
        this.script = script;
        this.scriptStatus = ScriptStatus.READY;
    }

    public LuaScript toFile(File f) {
        throw new RuntimeException("Not Implemented");
    }

    public synchronized LuaScript start() {
        thread = new Thread(() -> {
            scriptStatus = ScriptStatus.RUNNING;
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
        return scriptStatus;
    }

    public synchronized LuaScript resume() {
        throw new RuntimeException("Not Implemented");
    }

    public synchronized LuaScript pause() {
        throw new RuntimeException("Not Implemented");
    }

    public synchronized LuaScript join() {
        return join(0);
    }

    public synchronized LuaScript join(long wait) {
        try {
            thread.getState();
            thread.join(wait);
            if(thread.isAlive())
                scriptStatus = ScriptStatus.ERROR;
            else
                scriptStatus = ScriptStatus.COMPLETE;
            return this;
        }
        catch (Exception e) {
            scriptStatus = ScriptStatus.ERROR;
            return this;
        }
    }

    public synchronized Optional<Varargs> getResults() {
        return Optional.ofNullable(varargs);
    }
}
