package com.undead_pixels.dungeon_bots.script.interfaces;
import com.undead_pixels.dungeon_bots.script.Whitelist;
import com.undead_pixels.dungeon_bots.utils.annotations.BindField;
import com.undead_pixels.dungeon_bots.utils.annotations.BindMethod;
import com.undead_pixels.dungeon_bots.utils.annotations.BindTo;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.undead_pixels.dungeon_bots.script.LuaSandbox.id;
import static com.undead_pixels.dungeon_bots.script.LuaSandbox.staticId;

public interface LuaReflection {

	default Whitelist permissiveWhitelist() {
		return new Whitelist()
				.addTo(this.getBindableMethods().stream()
						.filter(method -> method.getDeclaredAnnotation(BindMethod.class) != null)
						.map(this::genId)
						.distinct()
						.collect(Collectors.toList()));
	}

	default Collection<Method> getBindableStaticMethods() {
		List<Method> m = Stream.of(this.getClass().getDeclaredMethods()).collect(Collectors.toList());
		Class<?> clz = this.getClass().getSuperclass();
		while (!clz.equals(Object.class)) {
			m.addAll(Stream.of(clz.getDeclaredMethods()).collect(Collectors.toSet()));
			clz = clz.getSuperclass();
		}
		return m.stream()
				.filter(method -> method.getDeclaredAnnotation(BindMethod.class)
						!= null && Modifier.isStatic(method.getModifiers()))
				.collect(Collectors.toList());
	}


	default Collection<Method> getBindableMethods() {
		List<Method> m = Stream.of(this.getClass().getDeclaredMethods()).collect(Collectors.toList());
		Class<?> clz = this.getClass().getSuperclass();
		while (!clz.equals(Object.class)) {
			m.addAll(Stream.of(clz.getDeclaredMethods()).collect(Collectors.toSet()));
			clz = clz.getSuperclass();
		}
		return m.stream()
				.filter(method -> method.getDeclaredAnnotation(BindMethod.class) != null
						&& !Modifier.isStatic(method.getModifiers()))
				.collect(Collectors.toList());
	}

	default Collection<Method> getBindableMethods(SecurityLevel securityLevel) {
		Set<Method> m = Stream.of(this.getClass().getDeclaredMethods()).collect(Collectors.toSet());
		Class<?> clz = this.getClass().getSuperclass();
		while (!clz.equals(Object.class)) {
			m.addAll(Stream.of(clz.getDeclaredMethods()).collect(Collectors.toSet()));
			clz = clz.getSuperclass();
		}
		return m.stream()
				.filter(method -> {
					BindMethod annotation = method.getDeclaredAnnotation(BindMethod.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& !Modifier.isStatic(method.getModifiers());
				})
				.collect(Collectors.toList());
	}

	default Collection<Method> getBindableStaticMethods(SecurityLevel securityLevel) {
		Set<Method> m = Stream.of(this.getClass().getDeclaredMethods()).collect(Collectors.toSet());
		Class<?> clz = this.getClass().getSuperclass();
		while (!clz.equals(Object.class)) {
			m.addAll(Stream.of(clz.getDeclaredMethods()).collect(Collectors.toSet()));
			clz = clz.getSuperclass();
		}
		return m.stream()
				.filter(method -> {
					BindMethod annotation = method.getDeclaredAnnotation(BindMethod.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& Modifier.isStatic(method.getModifiers());
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
	default String genStaticId(Method m) { return staticId(m); }

	default String genId(Field f) {
		return id(this, f);
	}

}
