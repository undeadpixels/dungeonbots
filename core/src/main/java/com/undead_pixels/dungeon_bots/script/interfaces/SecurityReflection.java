package com.undead_pixels.dungeon_bots.script.interfaces;
import com.undead_pixels.dungeon_bots.script.Whitelist;
import com.undead_pixels.dungeon_bots.utils.annotations.BindMethod;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public interface SecurityReflection {

	default Whitelist generateWhitelist() {
		return new Whitelist()
				.addTo(Stream.of(this.getClass().getDeclaredMethods())
						.filter(method -> method.getDeclaredAnnotation(BindMethod.class) != null)
						.map(Method::getName)
						.distinct()
						.toArray());
	}
}
