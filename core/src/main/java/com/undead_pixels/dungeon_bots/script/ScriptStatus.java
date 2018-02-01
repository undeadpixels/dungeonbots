package com.undead_pixels.dungeon_bots.script;

/**
 * @author Stewart Charles
 * @version 2/1/2018
 * An enumeration of the various status of a LuaScript
 */
public enum ScriptStatus {
    /**
     * The LuaScript has not been invoked
     */
    READY,
    /**
     * The LuaScript is currently running
     */
    RUNNING,
    /**
     * The LuaScript has been requested to STOP
     */
    STOPPED,
    /**
     * The execution of the script was interrupted with a LuaError exception
     */
    LUA_ERROR,
    /**
     * The LuaScript object has thrown an Error
     */
    ERROR,
    /**
     * The LuaScript has timed out when asked to join after a specified amount of time
     */
    TIMEOUT,
    /**
     * The LuaScript has been paused
     */
    PAUSED,
    /**
     * The LuaScript has completed execution
     */
    COMPLETE
}
