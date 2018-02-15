package com.undead_pixels.dungeon_bots;

import static org.junit.Assert.*;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;



public class JCodeREPLTest {

	@Test
	public void testExecution() {
		LuaSandbox luaSandbox = new LuaSandbox();
        JCodeREPL editor = new JCodeREPL(luaSandbox);
        editor.setCode("x = 2 + 1; return x;");
        assertFalse(editor.getMessages().contains("3"));      
        
        editor.executeSynchronized(100);
        luaSandbox.getQueue().update(0.0f);
        String message = editor.getMessages();
        assertTrue(message.contains("3"));

	}

}
