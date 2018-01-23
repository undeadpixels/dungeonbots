package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.builders.ActorBuilder;
import org.junit.*;

public class TestWhitelist {

	@Test
	public void testGetWhitelist() {
		Actor a = new ActorBuilder().createActor();
		Whitelist w = a.getWhitelist();
		Assert.assertTrue(w.onWhitelist("up"));
		Assert.assertTrue(w.onWhitelist("down"));
		Assert.assertTrue(w.onWhitelist("left"));
		Assert.assertTrue(w.onWhitelist("right"));
	}

	@Test
	public void testMethodNotOnWhitelist() {
		Actor a = new ActorBuilder().setWhitelist(new Whitelist().addTo("up")).createActor();
		Whitelist w = a.getWhitelist();
		Assert.assertTrue(w.onWhitelist("up"));
		Assert.assertFalse(w.onWhitelist("down"));
		Assert.assertFalse(w.onWhitelist("left"));
		Assert.assertFalse(w.onWhitelist("right"));
	}

	@Test
	public void testRemoveFromWhitelist() {
		Actor a = new ActorBuilder().setName("test").createActor();
		LuaScriptEnvironment scriptEnvironment = a.getScriptEnvironment(SecurityLevel.DEBUG);
		Whitelist w = a.getWhitelist();
		Assert.assertTrue(w.onWhitelist("up"));

		LuaScript luaScript = scriptEnvironment.init("test.up()").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		Assert.assertEquals( 1.0, a.getPosition().y, 0.001);

		w.removeFrom("up");
		luaScript = scriptEnvironment.init("test.up()").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.LUA_ERROR);
		Assert.assertTrue(luaScript.getError().getMessage().contains("Method 'up' has not been whitelisted"));
	}

	@Test
	public void testAddToWhitelist() {
		Actor a = new ActorBuilder().setName("test").setWhitelist(new Whitelist()).createActor();
		LuaScriptEnvironment scriptEnvironment = a.getScriptEnvironment(SecurityLevel.DEBUG);
		Whitelist w = a.getWhitelist();

		LuaScript luaScript = scriptEnvironment.init("test.up()").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.LUA_ERROR);
		Assert.assertTrue(luaScript.getError().getMessage().contains("Method 'up' has not been whitelisted"));

		w.addTo("up");
		luaScript = scriptEnvironment.init("test.up()").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		Assert.assertEquals(a.getPosition().y, 1.0, 0.001);
	}
}
