package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Setup {
	public static List<Class<? extends GetLuaFacade>> getItemClasses() {
		return getClassesOf(Item.class);
	}

	public static List<Class<? extends GetLuaFacade>> getEntityClasses() {
		return getClassesOf(Entity.class);
	}

	public static List<Class<? extends GetLuaFacade>> getClassesOf(Class<? extends GetLuaFacade> clz) {
		final List<Class<? extends GetLuaFacade>> ans = new LinkedList<>();
		final FastClasspathScanner scanner = new FastClasspathScanner();
		scanner.matchSubclassesOf(clz, e -> {
			System.out.println(e.getSimpleName());
			ans.add(e);
		}).scan();
		return ans;
	}
}