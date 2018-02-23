package com.undead_pixels.dungeon_bots.scene;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.file.editor.TileRegionSection;

public class EditorSaveTest {

	@Test
	public void simpleTest() {
		GameEditorState s = new GameEditorState(new World());
		Assert.assertNotEquals("", s.toLua());
		// System.out.println(s.toLua());
	}

	@Test
	public void slightlyLessSimpleTest() {
		GameEditorState s = new GameEditorState(new World());
		s.worldSizeSection.width = 50;
		s.worldSizeSection.height = 51;
		s.tileRegionSection.add(new TileRegionSection.TileRegion(5, 15, 5, 15, "floor"));
		s.tileRegionSection.add(new TileRegionSection.TileRegion(2, 2, 3, 3, "wall"));
		s.playerInitSection.px = 8;
		s.playerInitSection.py = 9;

		Assert.assertNotEquals("", s.toLua());
		// System.out.println(s.toLua());
	}

	@Test
	public void actualSerializeDeserializeTest() throws ParseException {
		GameEditorState s = new GameEditorState(new World());
		s.worldSizeSection.width = 50;
		s.worldSizeSection.height = 51;
		s.tileRegionSection.add(new TileRegionSection.TileRegion(5, 15, 5, 15, "floor"));
		s.tileRegionSection.add(new TileRegionSection.TileRegion(2, 2, 3, 3, "wall"));
		s.playerInitSection.px = 8;
		s.playerInitSection.py = 9;

		String luaOut = s.toLua();
		System.out.println("1.\n" + luaOut);

		GameEditorState s2 = new GameEditorState(new World(), luaOut);

		String luaOut2 = s2.toLua();
		System.out.println("2.\n" + luaOut2);

		Assert.assertEquals("Check if serialize->deserialize->serialize is consistent", s.toLua(), luaOut2);
	}

	@Test
	public void testSerializeWorld() throws Exception {
		testWorldMadeFromScript("level1.lua", false);
		testWorldMadeFromScript("maze1.lua", false);
		testWorldMadeFromScript("maze2.lua", false);
		testWorldMadeFromScript("default.lua", false);
	}

	private static void testWorldMadeFromScript(String filename, boolean printResults) throws Exception {

		World w1 = new World(new File(filename));
		World w2 = Serializer.deserializeWorld(Serializer.serializeWorld(w1));

		Serializer.validate(w1, w2, filename + " world", false, true, printResults);
	}
	
	@Test
	public void testSerializeLevelPack() {
		
	}
	
	

	
}
