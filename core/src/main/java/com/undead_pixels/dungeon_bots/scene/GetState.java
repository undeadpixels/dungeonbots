package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface GetState {
	default Map<String, Object> getState() {
		return Stream.of(this.getFields())
				.collect(HashMap::new,
						(m, f) -> m.put(f.getName(), f),
						HashMap::putAll);
	}

	default Field[] getFields() {
		return (Field[]) LuaReflection
				.flattenClass(this.getClass(), Class::getDeclaredFields)
				.filter(f -> f.getDeclaredAnnotation(State.class) != null)
				.map(f -> {
					if(LuaReflection.collectClasses(this.getClass())
							.map(Class::getInterfaces)
							.flatMap(Stream::of)
							.anyMatch(GetState.class::equals)) {
						try {
							return GetState.class.cast(f.get(this)).getFields();
						}
						catch (Exception e) { }
					}
					return new Field[] {f}; })
				.flatMap(Stream::of)
				.toArray();
	}
}
