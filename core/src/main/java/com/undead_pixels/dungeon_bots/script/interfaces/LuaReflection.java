package com.undead_pixels.dungeon_bots.script.interfaces;
import com.undead_pixels.dungeon_bots.script.Whitelist;
import com.undead_pixels.dungeon_bots.utils.annotations.BindField;
import com.undead_pixels.dungeon_bots.utils.annotations.BindMethod;
import com.undead_pixels.dungeon_bots.utils.annotations.BindTo;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.undead_pixels.dungeon_bots.script.LuaSandbox.id;

public interface LuaReflection {

	default Whitelist permissiveWhitelist() {
		return new Whitelist()
				.addTo(Stream.of(this.getClass().getDeclaredMethods())
						.filter(method -> method.getDeclaredAnnotation(BindMethod.class) != null)
						.map(this::genId)
						.distinct()
						.collect(Collectors.toList()));
	}

	default Collection<Method> getBindableMethods() {
		return Stream.of(this.getClass().getDeclaredMethods())
				.filter(method -> method.getDeclaredAnnotation(BindMethod.class) != null)
				.collect(Collectors.toList());
	}

	default Collection<Method> getBindableMethods(SecurityLevel securityLevel) {
		return Stream.of(this.getClass().getDeclaredMethods())
				.filter(method -> {
					BindMethod annotation =  method.getDeclaredAnnotation(BindMethod.class);
					return annotation != null && annotation.value().level <= securityLevel.level;
				})
				.collect(Collectors.toList());
	}

	default Collection<Method> getBindableFields() {
		return Stream.of(this.getClass().getDeclaredMethods())
				.filter(method -> method.getDeclaredAnnotation(BindField.class) != null)
				.collect(Collectors.toList());
	}

	default Collection<Field> getBindableFields(SecurityLevel securityLevel) {
		return Stream.of(this.getClass().getDeclaredFields())
				.filter(field -> {
					BindField annotation =  field.getDeclaredAnnotation(BindField.class);
					return annotation != null && annotation.value().level <= securityLevel.level;
				})
				.collect(Collectors.toList());
	}

	static String bindTo(Method m) {
		return Optional.ofNullable(m.getDeclaredAnnotation(BindTo.class))
				.map(BindTo::value)
				.orElse(m.getName());
	}

	static String bindTo(Field m) {
		return Optional.ofNullable(m.getDeclaredAnnotation(BindTo.class))
				.map(BindTo::value)
				.orElse(m.getName());
	}

	default String genId(Method m) {
		return id(this, m);
	}

	default String genId(Field f) {
		return id(this, f);
	}

}
