package com.undead_pixels.dungeon_bots.script.proxy;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Stewart Charles
 * @version 2/1/2018
 * Class containing static methods that handle many common reflection related operations<br>
 * used for retrieving annotated methods and fields for creating Lua code that can invoke java methods
 */
public class LuaReflection {
	/**
	 * Generates a Whitelist of Methods for the target object not exceeding the given security level.
	 * @param bindableMethods An argument stream of bindable methods
	 * @param caller The object that invokes the bindable methods
	 * @param securityLevel The desired Security Level of the whitelist
	 * @return A Whitelist that contains ID's of all bindable methods matching the security level
	 */
	public static Whitelist getWhitelist(final Stream<Method> bindableMethods, final Object caller, final SecurityLevel securityLevel) {
		return new Whitelist()
				.addId(bindableMethods.filter(method ->
						method.getDeclaredAnnotation(Bind.class) != null
								&& method.getDeclaredAnnotation(Bind.class).value().level <= securityLevel.level)
						.map(method ->genId(caller, method)));
	}

	/**
	 * Generates a Whitelist of Methods for the target class not exceeding the given security level.
	 * @param bindableMethods
	 * @param securityLevel
	 * @return
	 */
	public static Whitelist getWhitelist(final Stream<Method> bindableMethods, final SecurityLevel securityLevel) {
		return new Whitelist()
				.addId(bindableMethods.filter(method ->
						method.getDeclaredAnnotation(Bind.class) != null
								&& method.getDeclaredAnnotation(Bind.class).value().level <= securityLevel.level)
						.map(method ->genId(null, method)));
	}

	/**
	 * Generates a unique String ID for the Member of the source Object
	 * @param o An object, can be null for static Members
	 * @param m A member (Field or Method)
	 * @return A String ID that specifically refers to the member of the specific object
	 */
	public static String genId(final Object o, final Member m) {
		return Optional.ofNullable(o).map(val -> Integer.toString(val.hashCode())).orElse("")
				+ Integer.toString(m.hashCode());
	}

	/**
	 * Returns a collection of Bindable methods found for the argument object.
	 * A Bindable method is any method that has been tagged with the @Bind annotation.
	 * @param o The Object to get all Bindable methods of
	 * @return A Stream of the Objects Bindable Methods
	 */
	public static Stream<Method> getBindableMethods(final Object o) {
		return getAllMethods(o.getClass())
				.filter(method -> {
					Bind annotation = method.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& !Modifier.isStatic(method.getModifiers()); })
				.sequential()
				.collect((Supplier<HashMap<String, Method>>) HashMap::new,
						// Collect methods in order of most specific class to least
						(map, method) -> {
							String name = GetLuaFacade.bindTo(method);
							if(!map.containsKey(name))
								map.put(name, method);
						},
						HashMap::putAll)
				.values().stream();
	}

	/**
	 * A method that returns a stream of methods belonging to the target class<br>
	 *     that have been annotated with the @Bind annotation that are static.
	 * @param c The Class to collect Bindable methods for
	 * @return A Stream of Methods
	 */
	public static Stream<Method> getBindableStaticMethods(final Class<?> c) {
		return getAllMethods(c)
				.filter(method -> {
					Bind annotation = method.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& Modifier.isStatic(method.getModifiers()); });
	}

	/**
	 * A method that returns a stream of fields belonging to the target class<br>
	 *     that have been annotated with the @Bind annotation
	 * @param c The target Class to get the bindable fields of
	 * @return A Stream of Fields
	 */
	public static Stream<Field> getBindableFields(final Class<?> c) {
		return getAllFields(c)
				.filter(field -> {
					Bind annotation = field.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& !Modifier.isStatic(field.getModifiers()); });
	}

	/**
	 * A method that returns a stream of methods belonging to the target class<br>
	 *     that have been annotated with the @Bind annotation that are static.
	 * @param c The target Class to get the bindable static fields of
	 * @return A Stream of Fields
	 */
	public static Stream<Field> getBindableStaticFields(final Class<?> c) {
		return getAllFields(c)
				.filter(field -> {
					Bind annotation = field.getDeclaredAnnotation(Bind.class);
					return annotation != null
							&& Modifier.isStatic(field.getModifiers()); });
	}

	/**
	 * Finds and possibly returns the first instance of a method belonging to the <br>
	 *     target object that has the specified name.
	 * @param o The target object
	 * @param name The name of the method to find
	 * @return An optional that possibly contains the Method if it is found.
	 */
	public static Optional<Method> getMethodWithName(Object o, String name) {
		return getBindableMethods(o)
				.filter(m -> GetLuaFacade.bindTo(m).equals(name))
				.findFirst();
	}

	private static Stream<Method> getAllMethods(final Class<?> clz) {
		return flattenClass(clz, Class::getDeclaredMethods);
	}

	private static Stream<Field> getAllFields(final Class<?> clz) {
		return flattenClass(clz, Class::getDeclaredFields);
	}

	public static Stream<Class<?>> collectClasses(final Class<?> src) {
		final Collection<Class<?>> classes = new ArrayList<>();
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

	public static <T extends Member> Stream<T> flattenClass(final Class<?> src, final Function<Class<?>,T[]> fn) {
		return collectClasses(src)
				.map(fn)
				.flatMap(Stream::of)
				.sequential();
	}
}
