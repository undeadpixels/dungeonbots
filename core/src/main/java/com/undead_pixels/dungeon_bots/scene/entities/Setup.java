package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.treasure.*;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.*;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;

import java.util.Arrays;
import java.util.List;

public class Setup {
	public static List<Class<? extends GetLuaFacade>> getItemClasses() {
		return Arrays.asList(
				Diamond.class,
				Gem.class,
				Gold.class,
				Bow.class,
				Sword.class,
				SpellBook.class,
				Key.class,
				MultipleChoiceQuestion.class,
				Note.class,
				ResponseQuestion.class,
				Website.class);
	}
}