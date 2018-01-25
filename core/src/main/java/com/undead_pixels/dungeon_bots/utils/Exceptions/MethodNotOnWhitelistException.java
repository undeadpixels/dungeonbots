package com.undead_pixels.dungeon_bots.utils.Exceptions;

import java.lang.reflect.Method;

public class MethodNotOnWhitelistException extends Exception {
	public MethodNotOnWhitelistException(Method m) {
		super(String.format("Method '%s' has not been whitelisted", m.getName()));
	}
}
