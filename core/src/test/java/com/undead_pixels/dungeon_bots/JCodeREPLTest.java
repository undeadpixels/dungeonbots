package com.undead_pixels.dungeon_bots;

import static org.junit.Assert.*;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import org.junit.Test;

import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;



public class JCodeREPLTest {

	@Test
	public void testExecution() {
		LuaSandbox luaSandbox = new LuaSandbox();
		JCodeREPL editor = new JCodeREPL(luaSandbox);
		editor.setCode("x = 2 + 1;\nreturn x;");
		assertFalse("Before execution, editor messages should not contain 3", editor.getMessages().contains("3"));

		editor.execute().join(500);
		try {
			Thread.sleep(50); // wait for the message to actually show up in the repl
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		luaSandbox.getQueue().update(0.0f);
		String message = editor.getMessages();
		System.out.println("Message = "+message);
		assertTrue(String.format("Message %s does not contain 3", message), message.contains("3"));

	}

}
