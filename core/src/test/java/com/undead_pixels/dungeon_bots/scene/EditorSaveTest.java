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
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.file.Serializer.PrintOptions;
import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.file.editor.TileRegionSection;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.scene.level.WorldList;

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
		testWorldMadeFromScript("level1.lua", Serializer.PrintOptions.NONE);
		testWorldMadeFromScript("maze1.lua", Serializer.PrintOptions.NONE);
		testWorldMadeFromScript("maze2.lua", Serializer.PrintOptions.NONE);
		testWorldMadeFromScript("default.lua", Serializer.PrintOptions.NONE);
	}

	@Test
	public void testSerializeWorldList() throws Exception {
		World w1 = new World(new File("level1.lua"));
		World w2 = new World(new File("maze1.lua"));
		World w3 = new World(new File("maze2.lua"));

		WorldList original = new WorldList();
		original.add(w1);
		original.add(w2);
		original.add(w3);

		byte[] rawBytes = Serializer.serializeWorlds(original);
		WorldList copy = Serializer.deserializeWorlds(rawBytes);
		Serializer.validate(original, copy, "WorldList original", false, true, PrintOptions.ALL_NON_MATCHED);

		// Take through string encoding.
		String encodedString = Base64.getEncoder().encodeToString(rawBytes);
		byte[] copyBytes = Base64.getDecoder().decode(encodedString.getBytes());
		for (int i = 0; i < copyBytes.length; i++) {
			try {
				assert rawBytes[i] == copyBytes[i];
			} catch (Error e) {
				System.out.println(i);
			}
		}
		copy = Serializer.deserializeWorlds(copyBytes);
		Serializer.validate(original, copy, "WorldList original", false, true, PrintOptions.ALL_NON_MATCHED);

	}

	private static void testWorldMadeFromScript(String filename, Serializer.PrintOptions options) throws Exception {

		World w1 = new World(new File(filename));
		World w2 = Serializer.deserializeWorld(Serializer.serializeWorld(w1));

		Serializer.validate(w1, w2, filename + " world", false, true, options);
	}

	@Test
	public void testSerializeLevelPack() throws Exception {
		LevelPack lp1 = new LevelPack("lp01", User.dummy());
		String serialized = Serializer.serializeLevelPack(lp1);
		LevelPack lp2 = Serializer.deserializeLevelPack(Serializer.serializeLevelPack(lp1));
		Serializer.validate(lp1, lp2, lp1.getName() + " LevelPack", false, true,
				Serializer.PrintOptions.ALL_NON_MATCHED);

		// Test save and load.
		lp1.toFile("example.json");
		lp2 = LevelPack.fromFile("example.json");
		Serializer.validate(lp1, lp2, lp1.getName() + " LevelPack", false, true,
				Serializer.PrintOptions.ALL_NON_MATCHED);

	}

}
