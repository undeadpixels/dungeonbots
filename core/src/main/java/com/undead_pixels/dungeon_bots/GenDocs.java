package com.undead_pixels.dungeon_bots;

import com.google.gson.GsonBuilder;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public final class GenDocs {

	public final static class DocMethodParam {
		String type;
		String descr;

		DocMethodParam(String name, String descr) {
			this.type = name;
			this.descr = descr;
		}
	}

	public final static class DocMethod {
		String name;
		String descr;
		String role;
		List<DocMethodParam> params = new LinkedList<>();

		DocMethod(String name, String descr, String role) {
			this.name = name;
			this.descr = descr;
			this.role = role;
		}
	}

	public final static class DocClass {
		String name;
		String descr;
		List<DocMethod> methods = new LinkedList<>();

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
										Optional.ofNullable(clz.getDeclaredAnnotation(Doc.class))
												.map(Doc::value)
												.orElse(""))))
				.matchClassesWithMethodAnnotation(Doc.class, (clz, fn) -> {
					final DocClass docClass = ans.computeIfAbsent(
							clz.getSimpleName(),
							(v) -> new DocClass(clz.getSimpleName(), ""));
					final DocMethod docMethod = new DocMethod(
							GetLuaFacade.bindTo(fn),
							fn.getDeclaredAnnotation(Doc.class).value(),
							Optional.ofNullable(fn.getDeclaredAnnotation(Bind.class))
									.map(a -> a.value().name()).orElse("NONE"));
					Stream.of(fn.getParameters())
							.forEach(p -> docMethod.params.add(
									new DocMethodParam(p.getType().getSimpleName(),
											Optional.ofNullable(p.getDeclaredAnnotation(Doc.class))
													.map(Doc::value)
													.orElse(""))));
					docClass.methods.add(docMethod); })
				.scan();

		return ans.values();
	}
}
