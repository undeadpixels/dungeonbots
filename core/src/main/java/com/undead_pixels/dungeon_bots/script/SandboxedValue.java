package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import org.luaj.vm2.Varargs;

import java.util.function.Function;

public class SandboxedValue {
	private final Varargs varargs;
	private final LuaSandbox luaSandbox;

	public SandboxedValue(Varargs varargs, LuaSandbox luaSandbox) {
		this.varargs = varargs;
		this.luaSandbox = luaSandbox;
	}

	public Varargs getResult() {
		return varargs;
	}

	public LuaSandbox getLuaSandbox() {
		return luaSandbox;
	}

	/**
	 * Safely invokes the underlying LuaValue in its Sandbox Context.
	 * @param fn
	 * @return
	 */
	public SandboxedValue invoke(Function<Varargs,Varargs> fn) {
		// TODO - is this still valid even?
		return new SandboxedValue(fn.apply(varargs), luaSandbox);
	}
}
