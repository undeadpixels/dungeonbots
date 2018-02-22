package com.undead_pixels.dungeon_bots.utils.generic;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Tuple {
	private final List<Object> tuple;

	public Tuple(Object... objects) {
		this.tuple = Arrays.asList(objects);
	}

	public <T> Optional<T> get(int index, Class<T> type) {
		Object o = this.tuple.get(index);
		if(type.isAssignableFrom(o.getClass()))
			return Optional.of(type.cast(o));
		return Optional.empty();
	}
}
