package com.undead_pixels.dungeon_bots;

import org.junit.Assert;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.file.GameEditorState;

public class EditorSaveTest {

    @Test
	public void simpleTest() {
		GameEditorState s = new GameEditorState();
		Assert.assertNotEquals("", s.toLua());
		System.out.println(s.toLua());
	}
}
