package com.undead_pixels.dungeon_bots.scene;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.file.editor.TileRegionSection;

public class EditorSaveTest {

	@Test
	public void simpleTest() {
		GameEditorState s = new GameEditorState(new World());
		Assert.assertNotEquals("", s.toLua());
		System.out.println(s.toLua());
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
		System.out.println(s.toLua());
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
		System.out.println("1.\n"+luaOut);
		
		GameEditorState s2 = new GameEditorState(new World(), luaOut);

		String luaOut2 = s2.toLua();
		System.out.println("2.\n"+luaOut2);
		
		Assert.assertEquals("Check if serialize->deserialize->serialize is consistent", s.toLua(), luaOut2);
	}


	@Test
	public void simpleJavaSerializeDeserializeTest() throws ParseException, IOException, ClassNotFoundException {
		
		World w = new World();

		PipedInputStream in_ = new PipedInputStream();
		PipedOutputStream out_ = new PipedOutputStream(in_);
		ObjectOutputStream out = new ObjectOutputStream(out_);
		ObjectInputStream in = new ObjectInputStream(in_);
		
		//out.writeObject(w);

		//World w2 = (World) in.readObject();
		
		out.close();
	}
}
