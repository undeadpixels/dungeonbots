package com.undead_pixels.dungeon_bots.utils.exceptions;

import java.lang.reflect.Method;

public class MethodNotOnWhitelistException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MethodNotOnWhitelistException(Method m) {
		this(m.getName());
	}
	public MethodNotOnWhitelistException(String str) {
		super(String.format("Method '%s' has not been whitelisted", str));
	}
}
