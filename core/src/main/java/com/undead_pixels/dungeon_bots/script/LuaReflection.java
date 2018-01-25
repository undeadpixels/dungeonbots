package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LuaReflection {

	public static Whitelist getPermissiveWhitelist(Collection<Method> bindableMethods) {
		return new Whitelist()
				.addTo(bindableMethods.stream()
						.filter(method -> method.getDeclaredAnnotation(Bind.class) != null)
						.map(method ->genId(null, method) )
						.distinct()
						.collect(Collectors.toList()));
	}

	public static Whitelist getPermissiveWhitelist(Collection<Method> bindableMethods, Object caller) {
		return new Whitelist()
				.addTo(bindableMethods.stream()
						.filter(method -> method.getDeclaredAnnotation(Bind.class) != null)
						.map(method ->genId(caller,method) )
						.distinct()
						.collect(Collectors.toList()));
	}

	public static String genId(Object o, Method m) {
		return Optional.ofNullable(o).map(val -> Integer.toString(val.hashCode())).orElse("")
				+ m.toGenericString();
	}

	public static Collection<Method> getBindableMethods(Object o, SecurityLevel securityLevel) {
		return getAllMethods(o.getClass()).stream()
				.filter(method -> {
					Bind annotation = method.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& !Modifier.isStatic(method.getModifiers());
				})
				.collect(Collectors.toList());
	}

	public static Collection<Method> getBindableStaticMethods(Class<?> c, SecurityLevel securityLevel) {
		return getAllMethods(c).stream()
				.filter(method -> {
					Bind annotation = method.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& Modifier.isStatic(method.getModifiers());
				})
				.collect(Collectors.toList());
	}

	private static Collection<Method> getAllMethods(Class<?> c) {
		Collection<Collection<Method>> ans = new ArrayList<>();
		while(!c.equals(Object.class)) {
			ans.add(Stream.of(c.getDeclaredMethods()).collect(Collectors.toList()));
			c = c.getSuperclass();
		}
		return ans.stream().flatMap(Collection::stream).collect(Collectors.toList());
	}

	private static Collection<Field> getAllFields(Class<?> c) {
		Collection<Collection<Field>> ans = new ArrayList<>();
		while(!c.equals(Object.class)) {
			ans.add(Stream.of(c.getDeclaredFields()).collect(Collectors.toList()));
			c = c.getSuperclass();
		}
		return ans.stream().flatMap(Collection::stream).collect(Collectors.toList());
	}

	public static Collection<Field> getBindableFields(Class<?> c, SecurityLevel securityLevel) {
		return getAllFields(c).stream()
				.filter(field -> {
					Bind annotation = field.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& !Modifier.isStatic(field.getModifiers());
				}).collect(Collectors.toList());
	}

	public static Collection<Field> getBindableStaticFields(Class<?> c, SecurityLevel securityLevel) {
		return getAllFields(c).stream()
				.filter(field -> {
					Bind annotation = field.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& Modifier.isStatic(field.getModifiers());
				}).collect(Collectors.toList());
	}
}
