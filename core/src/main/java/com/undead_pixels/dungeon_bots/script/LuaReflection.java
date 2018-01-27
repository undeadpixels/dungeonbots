package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LuaReflection {

	/**
	 *
	 * @param bindableMethods
	 * @return
	 */
	public static Whitelist getPermissiveWhitelist(final Collection<Method> bindableMethods) {
		return new Whitelist()
				.addTo(bindableMethods.stream()
						.filter(method -> method.getDeclaredAnnotation(Bind.class) != null)
						.map(method ->genId(null, method) )
						.distinct()
						.collect(Collectors.toList()));
	}

	/**
	 *
	 * @param bindableMethods
	 * @param caller
	 * @return
	 */
	public static Whitelist getPermissiveWhitelist(final Collection<Method> bindableMethods, final Object caller) {
		return new Whitelist()
				.addTo(bindableMethods.stream()
						.filter(method -> method.getDeclaredAnnotation(Bind.class) != null)
						.map(method ->genId(caller,method) )
						.distinct()
						.collect(Collectors.toList()));
	}

	/**
	 *
	 * @param o
	 * @param m
	 * @return
	 */
	public static String genId(final Object o, final Method m) {
		return Optional.ofNullable(o).map(val -> Integer.toString(val.hashCode())).orElse("")
				+ m.toGenericString();
	}

	/**
	 *
	 * @param o
	 * @param securityLevel
	 * @return
	 */
	public static Stream<Method> getBindableMethods(final Object o, final SecurityLevel securityLevel) {
		return getAllMethods(o.getClass())
				.filter(method -> {
					Bind annotation = method.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& !Modifier.isStatic(method.getModifiers());
				});
	}

	/**
	 *
	 * @param c
	 * @param securityLevel
	 * @return
	 */
	public static Stream<Method> getBindableStaticMethods(final Class<?> c, final SecurityLevel securityLevel) {
		return getAllMethods(c)
				.filter(method -> {
					Bind annotation = method.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& Modifier.isStatic(method.getModifiers());
				});
	}

	private static Stream<Method> getAllMethods(final Class<?> clz) {
		Class<?> c = clz;
		final Map<String,Method> ans = new HashMap<>();
		while(!c.equals(Object.class)) {
			Stream.of(c.getDeclaredMethods())
					.forEach(method -> {
						String name = GetBindable.bindTo(method);
						if(!ans.containsKey(name))
							ans.put(name, method);
					});
			c = c.getSuperclass();
		}
		return ans.values().stream();
	}

	private static Stream<Field> getAllFields(final Class<?> clz) {
		Class<?> c = clz;
		Collection<Collection<Field>> ans = new ArrayList<>();
		while(!c.equals(Object.class)) {
			ans.add(Stream.of(c.getDeclaredFields()).collect(Collectors.toList()));
			c = c.getSuperclass();
		}
		return ans.stream().flatMap(Collection::stream);
	}

	public static Stream<Field> getBindableFields(final Class<?> c, final SecurityLevel securityLevel) {
		return getAllFields(c)
				.filter(field -> {
					Bind annotation = field.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& !Modifier.isStatic(field.getModifiers()); });
	}

	public static Stream<Field> getBindableStaticFields(final Class<?> c, final SecurityLevel securityLevel) {
		return getAllFields(c)
				.filter(field -> {
					Bind annotation = field.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& annotation.value().level <= securityLevel.level
							&& Modifier.isStatic(field.getModifiers()); });
	}
}
