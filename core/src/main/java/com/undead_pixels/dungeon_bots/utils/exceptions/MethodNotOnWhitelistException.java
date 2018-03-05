package com.undead_pixels.dungeon_bots.utils.exceptions;

import java.lang.reflect.Method;

public class MethodNotOnWhitelistException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MethodNotOnWhitelistException(Method m) {
		super(String.format("Method '%s' has not been whitelisted", m.getName()));
	}
}
