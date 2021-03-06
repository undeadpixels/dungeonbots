package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface GetState {
	default Map<String, Object> getState() {
		return this.getFields().stream()
				.collect(HashMap::new,
						(m, f) -> m.put(f.getName(), f),
						HashMap::putAll);
	}

	default List<Field> getFields() {
		final List<Field> s = LuaReflection
				.flattenClass(this.getClass(), Class::getDeclaredFields)
				.filter(f -> f.getDeclaredAnnotation(State.class) != null).collect(Collectors.toList());

		s.forEach(f -> {
			final Class<?> c = f.getType();
			if (LuaReflection.collectClasses(c)
					.map(Class::getInterfaces)
					.flatMap(Stream::of)
					.anyMatch(GetState.class::isInstance)) {
				try {
					s.addAll(new ArrayList<>(GetState.class.cast(f.get(this)).getFields()));
				} catch (Exception e) {
				}
			}
		});
		return s;
	}
}
