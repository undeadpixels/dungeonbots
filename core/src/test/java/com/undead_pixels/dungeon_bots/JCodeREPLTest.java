package com.undead_pixels.dungeon_bots;

import static org.junit.Assert.*;

import org.junit.Test;

import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.LuaScriptEnvironment;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;

import junit.framework.Assert;

public class JCodeREPLTest {

	@Test
	public void testExecution() {
		
        
        JCodeREPL editor = new JCodeREPL();
        editor.setCode("x = 2 + 1; return x;");
        assertFalse(editor.getMessages().contains("3"));      
        
        assertTrue(editor.execute(100));
        Object msgs = editor.getMessages();
        assertTrue(editor.getMessages().contains("3"));
        
        
	}

}
