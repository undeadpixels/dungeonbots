package com.undead_pixels.dungeon_bots;

import com.google.gson.GsonBuilder;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public final class GenDocs {

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
}
