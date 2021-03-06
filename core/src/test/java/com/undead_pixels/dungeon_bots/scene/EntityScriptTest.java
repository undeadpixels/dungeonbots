package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.*;
import com.undead_pixels.dungeon_bots.script.LuaInvocation;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.undead_pixels.dungeon_bots.script.ScriptStatus.*;

public class EntityScriptTest {

	@Test public void testEntitySandbox() {
		World w = new World();
		w.setSize(16,16);
		Player p = new Player(w, "player", 1, 1);
		w.addEntity(p);
		LuaInvocation luaScript = p.getSandbox().init("player:queueUp()").join();
		assertTrue(luaScript.getStatus() == COMPLETE);
		w.update(1.f);
		w.update(1.f);
		assertEquals(2.0, p.getPosition().y, 0.001);
	}

}
