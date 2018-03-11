package com.undead_pixels.dungeon_bots;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.undead_pixels.dungeon_bots.script.annotations.GenDoc;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.vladsch.flexmark.util.options.MutableDataSet;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public final class GenDocs {

	final static class DocMethodParam {
		String name;
		String descr;

		DocMethodParam(String name, String descr) {
			this.name = name;
			this.descr = descr;
		}
	}

	final static class DocMethod {
		String name;
		String descr;
		List<DocMethodParam> params = new LinkedList<>();

		DocMethod(String name, String descr) {
			this.name = name;
			this.descr = descr;
		}
	}

	final static class DocClass {
		String name;
		List<DocMethod> docMethods = new LinkedList<>();

		DocClass(String name) {
			this.name = name;
		}
	}

	public static void main(String[] args) {
		final Map<String,DocClass> ans = new HashMap<>();

		new FastClasspathScanner()
				.matchClassesWithMethodAnnotation(GenDoc.class, (clz, fn) -> {
					final DocClass docClass = ans.computeIfAbsent(clz.getName(), (v) -> new DocClass(clz.getName()));
					final DocMethod docMethod = new DocMethod(GetLuaFacade.bindTo(fn), fn.getDeclaredAnnotation(GenDoc.class).value());
					Stream.of(fn.getParameters())
							.forEach(p -> docMethod.params.add(
									new DocMethodParam(p.getName(),
									Optional.ofNullable(p.getDeclaredAnnotation(GenDoc.class)).map(GenDoc::value).orElse(""))));
					docClass.docMethods.add(docMethod);
				})
				.scan();
		try(Writer writer = new FileWriter("autodoc.json")) {
			new Gson().toJson(ans.values(), writer);
		}
		catch (IOException io) {
			System.out.println("Unable to create file");
		}
	}
}
