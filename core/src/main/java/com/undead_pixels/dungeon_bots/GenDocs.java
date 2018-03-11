package com.undead_pixels.dungeon_bots;

import com.google.gson.Gson;
import com.undead_pixels.dungeon_bots.script.annotations.GenDoc;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import java.io.*;
import java.util.*;

public final class GenDocs {

	public static void main(String[] args) {
		final MutableDataSet options = new MutableDataSet();
		final Parser parser = Parser.builder(options).build();
		final HtmlRenderer renderer = HtmlRenderer.builder(options).build();
		final Map<String,List<String>> output = new HashMap<>();

		new FastClasspathScanner()
				.matchClassesWithMethodAnnotation(GenDoc.class, (clz, fn) ->
					output.computeIfAbsent(clz.getName(), (v) -> new LinkedList<>())
							.add(renderer.render(
									parser.parse(fn.getDeclaredAnnotation(GenDoc.class).value()))))
				.scan();
		try(Writer writer = new FileWriter("autodoc.json")) {
			new Gson().toJson(output, writer);
		}
		catch (IOException io) {
			System.out.println("Unable to create file");
		}
	}
}
