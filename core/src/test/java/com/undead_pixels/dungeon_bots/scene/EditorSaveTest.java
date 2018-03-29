package com.undead_pixels.dungeon_bots.scene;

import java.io.File;
import java.util.Base64;

import org.junit.Test;

import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.file.Serializer.PrintOptions;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.scene.level.WorldList;

public class EditorSaveTest {

	@Test
	public void testSerializeWorld() throws Exception {
		testWorldReserialize("legacy_level1.json", Serializer.PrintOptions.NONE);
		testWorldReserialize("legacy_maze1.json", Serializer.PrintOptions.NONE);
		testWorldReserialize("legacy_maze2.json", Serializer.PrintOptions.NONE);
		testWorldReserialize("multigoals.json", Serializer.PrintOptions.NONE);
	}

	@Test
	public void testSerializeWorldList() throws Exception {
		World w1 = LevelPack.fromFile("legacy_level1.json").getCurrentWorld();
		World w2 = LevelPack.fromFile("legacy_maze1.json").getCurrentWorld();
		World w3 = LevelPack.fromFile("legacy_maze2.json").getCurrentWorld();

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

	private static void testWorldReserialize(String filename, Serializer.PrintOptions options) throws Exception {

		// TODO - probably should test the whole pack, instead of just worlds, now that this is reading json's
		World w1 = LevelPack.fromFile(filename).getCurrentWorld();
		w1.runInitScripts();
		World w2 = Serializer.deserializeWorld(Serializer.serializeWorld(w1));

		Serializer.validate(w1, w2, filename + " world", false, true, options);
	}

	@Test
	public void testSerializeLevelPack() throws Exception {
		LevelPack lp1 = new LevelPack("lp01", User.dummy());
		//String serialized = Serializer.serializeLevelPack(lp1);
		LevelPack lp2 = Serializer.deserializeLevelPack(Serializer.serializeLevelPack(lp1));
		Serializer.validate(lp1, lp2, lp1.getName() + " LevelPack", false, true,
				Serializer.PrintOptions.ALL_NON_MATCHED);

		// Test save and load.
		lp1.toFile("tmp/example.json");
		lp2 = LevelPack.fromFile("tmp/example.json");
		Serializer.validate(lp1, lp2, lp1.getName() + " LevelPack", false, true,
				Serializer.PrintOptions.ALL_NON_MATCHED);

	}

}
