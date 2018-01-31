package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.*;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.undead_pixels.dungeon_bots.script.ScriptStatus.*;

public class EntityScriptTest {

	@Test public void testEntitySandbox() {
		SecurityContext.set(SecurityLevel.DEBUG);
		World w = new World();
		Player p = new Player(w, "player");
		w.setSize(16,16);
		LuaScript luaScript = p.getSandbox().init("player:up()").join();
		assertTrue(luaScript.getStatus() == COMPLETE);
		w.update(1.f);
		w.update(1.f);
		assertEquals(1.0, p.getPosition().y, 0.001);
	}

}
