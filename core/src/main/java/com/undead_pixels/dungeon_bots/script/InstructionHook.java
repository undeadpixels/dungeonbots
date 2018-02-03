package com.undead_pixels.dungeon_bots.script;

import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;

/**
 * Based on https://stackoverflow.com/questions/17496868/lua-java-luaj-handling-or-interrupting-infinite-loops-and-threads?noredirect=1&lq=1
 */
public class InstructionHook extends DebugLib {
    public boolean interrupted = false;

    @Override
    public void onInstruction(int pc, Varargs v, int top) {
        if (interrupted) {
            throw new ScriptInterruptException();
        }
        super.onInstruction(pc, v, top);
    }

    public static class ScriptInterruptException extends RuntimeException {}
}