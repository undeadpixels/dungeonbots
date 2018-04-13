package com.undead_pixels.dungeon_bots;

import java.lang.reflect.Method;
import java.util.Optional;


public class TestWhitelist {

	private Optional<Method> findMethod(Object o, String name, Class<?>... parameterTypes) {
		try {
			return Optional.ofNullable(o.getClass().getDeclaredMethod(name, parameterTypes));
		}
		catch (Exception e) {
			return Optional.empty();
		}
	}

}
