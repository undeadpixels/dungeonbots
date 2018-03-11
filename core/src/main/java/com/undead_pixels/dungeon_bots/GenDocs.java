package com.undead_pixels.dungeon_bots;

import com.google.gson.GsonBuilder;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public final class GenDocs {

	public final static class DocMethodParam {
		public final String type;
		public final String descr;

		DocMethodParam(String name, String descr) {
			this.type = name;
			this.descr = descr;
		}
	}

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
		try(Writer writer = new FileWriter("autodoc.json")) {
			new GsonBuilder().setPrettyPrinting().create().toJson(getDocs(), writer);
		}
		catch (IOException io) {
			System.out.println("Unable to create file");
		}
	}

	public static Collection<DocClass> getDocs() {
		final Map<String,DocClass> ans = new HashMap<>();

		new FastClasspathScanner()
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
