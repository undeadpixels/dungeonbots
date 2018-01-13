package com.undead_pixels.dungeon_bots.script;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import org.luaj.vm2.LuaError;
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
        thread = ThreadWrapper.create(() -> {
            try {
                scriptStatus = ScriptStatus.RUNNING;
                LuaValue chunk = environment.getGlobals().load(this.script);
                varargs = chunk.invoke();
                scriptStatus = ScriptStatus.COMPLETE;
            } catch (LuaError le) {
                scriptStatus = ScriptStatus.LUA_ERROR;
            }
        });
        thread.start();
        return this;
    }

    /**
     * TODO: Actually stop the thread
     * @return
     */
    public synchronized LuaScript stop() {
        thread.interrupt();
        try {
            thread.join();
        }
        catch (InterruptedException ie) {
        }
        scriptStatus = ScriptStatus.STOPPED;
        return this;
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
            thread.join(wait);
            if(thread.isAlive())
                scriptStatus = ScriptStatus.TIMEOUT;
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
