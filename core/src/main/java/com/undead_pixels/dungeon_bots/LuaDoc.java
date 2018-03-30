package com.undead_pixels.dungeon_bots;

import com.google.gson.GsonBuilder;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.*;

public final class LuaDoc {

	public static final Collection<DocClass> DOCS = build();

	/* Create Simple container classes
	*  for game Documentation that
	*  serializes to JSON
	* */

	/**
	 * Container Class for Method Parameter Documentation
	 */
	public final static class DocMethodParam {
		public final String type;
		public final String descr;

		DocMethodParam(String name, String descr) {
			this.type = name;
			this.descr = descr;
		}
	}

	/**
	 * Container Class for Method Documentation
	 */
	public final static class DocMethod {
		public final String name;
		public final String descr;
		public final String role;
		public final List<DocMethodParam> params;

		DocMethod(String name, String descr, String role, List<DocMethodParam> params) {
			this.name = name;
			this.descr = descr;
			this.role = role;
			this.params = params;
		}
	}

	/**
	 * Container Class for Class Documentation
	 */
	public final static class DocClass {
		public final String name;
		public final String descr;
		public final List<DocMethod> methods = new LinkedList<>();

		DocClass(String name, String descr) {
			this.name = name;
			this.descr = descr;
		}
	}

	public static void main(String[] args) {
		toJson(build(), "autodoc.json");
	}

	private static boolean toJson(final Object o, final String toFile) {
		try(Writer writer = new FileWriter(toFile)) {
			new GsonBuilder().setPrettyPrinting().create().toJson(o, writer);
			return true;
		}
		catch (IOException io) {
			io.printStackTrace();
			return false;
		}
	}

	/**
	 * Collects and builds Documentation found throughout the entire source project
	 * @return A Collection of DocClass objects
	 */
	public static Collection<DocClass> build() {
		return build("");
	}

	/**
	 * Collects and builds Documentation found in the specified root src paths
	 * @param root A variadic list of source paths to scan for Documentation
	 * @return A Collection of DocClass objects
	 */
	public static Collection<DocClass> build(final String... root) {
		final Map<String,DocClass> ans = new HashMap<>();

		new FastClasspathScanner(root)
				.matchClassesWithAnnotation(Doc.class, (clz) ->
						ans.put(clz.getSimpleName(),
								new DocClass(
										clz.getSimpleName(),
										clz.getDeclaredAnnotation(Doc.class).value())))
				.matchClassesWithMethodAnnotation(Bind.class, (clz, fn) -> {
					final DocClass docClass = ans.computeIfAbsent(
							clz.getSimpleName(),
							(v) -> new DocClass(clz.getSimpleName(), ""));
					final Bind b = fn.getDeclaredAnnotation(Bind.class);
					Optional.of(b.doc())
							.filter(v -> !v.equals(""))
							.ifPresent(v -> docClass.methods.add(
									new DocMethod(GetLuaFacade.bindTo(fn),
											b.doc(),
											b.value().name(),
											Stream.of(fn.getParameters())
													.map(p -> new DocMethodParam(p.getType().getSimpleName(),
															Optional.ofNullable(p.getDeclaredAnnotation(Doc.class))
																	.map(Doc::value)
																	.orElse("")))
													.collect(Collectors.toList())))); })
				.matchClassesWithMethodAnnotation(Doc.class, (clz, fn) -> {
					final DocClass docClass = ans.computeIfAbsent(
							clz.getSimpleName(),
							(v) -> new DocClass(clz.getSimpleName(), ""));
					final DocMethod docMethod = new DocMethod(
							GetLuaFacade.bindTo(fn),
							fn.getDeclaredAnnotation(Doc.class).value(),
							Optional.ofNullable(fn.getDeclaredAnnotation(Bind.class))
									.map(a -> a.value().name())
									.orElse("NONE"),
							Stream.of(fn.getParameters())
									.map(p -> new DocMethodParam(p.getType().getSimpleName(),
											Optional.ofNullable(p.getDeclaredAnnotation(Doc.class))
													.map(Doc::value)
													.orElse("")))
									.collect(Collectors.toList()));
					docClass.methods.add(docMethod); })
				.scan();

		return ans.values();
	}

	public static  String docClassToString(final Class<?> clz) {
		return String.format(
				"---- %s -----\n__ %s __\n\n%s",
				clz.getSimpleName(),
				Optional.ofNullable(clz.getDeclaredAnnotation(Doc.class))
						.map(c -> c.value())
						.orElse(clz.getSuperclass().getSimpleName()),
				LuaReflection.getAllMethods(clz)
						.filter(m -> m.getDeclaredAnnotation(Bind.class) != null
								|| m.getDeclaredAnnotation(Doc.class ) != null)
						.map(m -> docMethodToString(m))
						.reduce("", (a,b) -> a + b));
	}

	private static String docMethodToString(final Method m) {
		final String name = GetLuaFacade.bindTo(m);
		final String doc = Optional.ofNullable(m.getDeclaredAnnotation(Doc.class))
				.map(a -> a.value())
				.orElse(Optional.ofNullable(m.getDeclaredAnnotation(Bind.class))
						.map(a -> a.doc())
						.orElse(""));
		final String access = Optional.ofNullable(m.getDeclaredAnnotation(Bind.class))
				.map(a -> a.value().name())
				.orElse("NONE");
		return String.format(
				"%s\t%s\n-- %s\n%s\n",
				name,
				access,
				doc,
				docParametersToString(m.getParameters()));
	}

	private static String docParametersToString(final Parameter[] parameters) {
		return Stream.of(parameters)
				.map(p -> String.format("\t%s: %s\n",
						p.getName(),
						Optional.ofNullable(p.getDeclaredAnnotation(Doc.class))
								.map(a -> a.value())
								.orElse("")))
				.reduce("", (a,b) -> a + b);
	}

	private static Stream<LuaValue> luaValueStream(final Varargs v) {
		List<LuaValue> ans = new ArrayList<>();
		for(int i = 1; i < v.narg(); i++) {
			ans.add(v.arg(i));
		}
		return ans.stream();
	}

}
