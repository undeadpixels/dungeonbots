package com.undead_pixels.dungeon_bots.script.proxy;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LuaReflection {

	/**
	 *
	 * @param bindableMethods
	 * @param caller
	 * @param securityLevel
	 * @return
	 */
	public static Whitelist getWhitelist(final Stream<Method> bindableMethods, final Object caller, final SecurityLevel securityLevel) {
		return new Whitelist()
				.addTo(bindableMethods.filter(method ->
						method.getDeclaredAnnotation(Bind.class) != null
								&& method.getDeclaredAnnotation(Bind.class).value().level <= securityLevel.level)
						.map(method ->genId(caller, method)));
	}

	/**
	 *
	 * @param bindableMethods
	 * @param securityLevel
	 * @return
	 */
	public static Whitelist getWhitelist(final Stream<Method> bindableMethods, final SecurityLevel securityLevel) {
		return new Whitelist()
				.addTo(bindableMethods.filter(method ->
						method.getDeclaredAnnotation(Bind.class) != null
								&& method.getDeclaredAnnotation(Bind.class).value().level <= securityLevel.level)
						.map(method ->genId(null, method)));
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
	 * @return
	 */
	public static Stream<Method> getBindableMethods(final Object o) {
		return getAllMethods(o.getClass())
				.filter(method -> {
					Bind annotation = method.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& !Modifier.isStatic(method.getModifiers()); });
	}

	/**
	 *
	 * @param c
	 * @return
	 */
	public static Stream<Method> getBindableStaticMethods(final Class<?> c) {
		return getAllMethods(c)
				.filter(method -> {
					Bind annotation = method.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& Modifier.isStatic(method.getModifiers()); });
	}

	/**
	 *
	 * @param c
	 * @return
	 */
	public static Stream<Field> getBindableFields(final Class<?> c) {
		return getAllFields(c)
				.filter(field -> {
					Bind annotation = field.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& !Modifier.isStatic(field.getModifiers()); });
	}

	/**
	 *
	 * @param c
	 * @return
	 */
	public static Stream<Field> getBindableStaticFields(final Class<?> c) {
		return getAllFields(c)
				.filter(field -> {
					Bind annotation = field.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& Modifier.isStatic(field.getModifiers()); });
	}

	private static Stream<Method> getAllMethods(final Class<?> clz) {
		return flattenClass(clz)
				.map(Class::getDeclaredMethods)
				.flatMap(Stream::of);
	}

	private static Stream<Field> getAllFields(final Class<?> clz) {
		return flattenClass(clz)
				.map(Class::getDeclaredFields)
				.flatMap(Stream::of).sequential();
	}

	private static Stream<Class<?>> flattenClass(final Class<?> src) {
		Collection<Class<?>> classes = new ArrayList<>();
		Class<?> temp = src;
		try {
			while (temp != null) {
				classes.add(temp);
				temp = temp.getSuperclass();
			}
		}
		catch (Exception e) { }
		return classes.stream().sequential();
	}

	public static Optional<Method> getMethodWithName(Object o, String name) {
		return getBindableMethods(o.getClass())
				.filter(m -> GetBindable.bindTo(m).equals(name))
				.findFirst();
	}
}
