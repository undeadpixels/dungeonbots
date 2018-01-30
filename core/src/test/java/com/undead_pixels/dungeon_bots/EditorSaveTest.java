package com.undead_pixels.dungeon_bots;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.file.GameEditorState;
import com.undead_pixels.dungeon_bots.file.TileRegionSection;

public class EditorSaveTest {

    @Test
	public void simpleTest() {
		GameEditorState s = new GameEditorState();
		Assert.assertNotEquals("", s.toLua());
		System.out.println(s.toLua());
	}

    @Test
	public void slightlyLessSimpleTest() {
		GameEditorState s = new GameEditorState();
		s.worldSizeSection.width = 50;
		s.worldSizeSection.height = 51;
		s.tileRegionSection.add(new TileRegionSection.TileRegion(5, 5, 15, 15, "floor"));
		s.tileRegionSection.add(new TileRegionSection.TileRegion(2, 2, 3, 3, "wall"));
		s.playerInitSection.px = 8;
		s.playerInitSection.py = 9;
		
		Assert.assertNotEquals("", s.toLua());
		System.out.println(s.toLua());
	}
    
    @Test
	public void actualSerializeDeserializeTest() throws ParseException {
		GameEditorState s = new GameEditorState();
		s.worldSizeSection.width = 50;
		s.worldSizeSection.height = 51;
		s.tileRegionSection.add(new TileRegionSection.TileRegion(5, 5, 15, 15, "floor"));
		s.tileRegionSection.add(new TileRegionSection.TileRegion(2, 2, 3, 3, "wall"));
		s.playerInitSection.px = 8;
		s.playerInitSection.py = 9;
		
		String luaOut = s.toLua();
		
		GameEditorState s2 = new GameEditorState(luaOut);

		String luaOut2 = s2.toLua();
		
		Assert.assertEquals("Check if serialize->deserialize->serialize is consistent", s.toLua(), luaOut2);
		System.out.println(s.toLua());
	}
}
