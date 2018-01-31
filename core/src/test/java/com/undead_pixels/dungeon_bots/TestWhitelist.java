package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import com.undead_pixels.dungeon_bots.utils.builders.ActorBuilder;
import org.junit.*;

import java.lang.reflect.Method;
import java.util.Optional;

public class TestWhitelist {

	private Optional<Method> findMethod(Object o, String name, Class<?>... parameterTypes) {
		try {
			return Optional.ofNullable(o.getClass().getDeclaredMethod(name, parameterTypes));
		}
		catch (Exception e) {
			return Optional.empty();
		}
	}

	@Test
	public void testGetWhitelist() {
		Actor a = new ActorBuilder().createActor();
		Whitelist w = a.getWhitelist(SecurityLevel.DEBUG);
		Optional<Method> m = findMethod(a, "up");
		Assert.assertTrue(m.isPresent() && w.onWhitelist(a, m.get()));
		m = findMethod(a, "down");
		Assert.assertTrue(m.isPresent() && w.onWhitelist(a, m.get()));
		m = findMethod(a, "left");
		Assert.assertTrue(m.isPresent() && w.onWhitelist(a, m.get()));
		m = findMethod(a, "right");
		Assert.assertTrue(m.isPresent() && w.onWhitelist(a, m.get()));
	}

	@Test
	public void testMethodNotOnWhitelist() {
		Actor a = new ActorBuilder().createActor();
		LuaSandbox sandbox = new LuaSandbox(SecurityLevel.NONE).addBindable(a);
		Whitelist w = sandbox.getWhitelist();
		Optional<Method> m = findMethod(a, "up");
		assert m.isPresent();
		w.add(a, m.get());
		Assert.assertTrue(m.isPresent() && w.onWhitelist(a, m.get()));
		m = findMethod(a, "down");
		Assert.assertFalse(m.isPresent() && w.onWhitelist(a, m.get()));
		m = findMethod(a, "left");
		Assert.assertFalse(m.isPresent() && w.onWhitelist(a, m.get()));
		m = findMethod(a, "right");
		Assert.assertFalse(m.isPresent() && w.onWhitelist(a, m.get()));
	}

	@Test
	public void testRemoveFromWhitelist() {
		Actor a = new ActorBuilder().setName("test").createActor();
		LuaSandbox scriptEnvironment = new LuaSandbox(SecurityLevel.DEBUG).addBindable(a);
		Whitelist w = scriptEnvironment.getWhitelist();
		Optional<Method> m = findMethod(a, "up");
		Assert.assertTrue(m.isPresent() && w.onWhitelist(a, m.get()));

		LuaScript luaScript = scriptEnvironment.init("test.up()").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		a.getWorld().setSize(16,16);
		a.getWorld().update(1.0f);
		Assert.assertEquals( 1.0, a.getPosition().y, 0.001);

		w.remove(a, m.get());
		luaScript = scriptEnvironment.init("test.up()").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.LUA_ERROR);
		Assert.assertTrue(luaScript.getError().getMessage().contains("Method 'up' has not been whitelisted"));
	}

	@Test
	public void testAddToWhitelist() {
		Actor a = new ActorBuilder().setName("test").createActor();
		LuaSandbox scriptEnvironment = new LuaSandbox(SecurityLevel.DEFAULT).addBindable(a);
		Whitelist w = scriptEnvironment.getWhitelist();

 		LuaScript luaScript = scriptEnvironment.init("test.up()").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.LUA_ERROR);
		Assert.assertTrue(luaScript.getError().getMessage().contains("Method 'up' has not been whitelisted"));

		Optional<Method> m = findMethod(a, "up");
		assert m.isPresent();
		w.add(a, m.get());
		luaScript = scriptEnvironment.init("test.up()").join();
		a.getWorld().setSize(16,16);
		a.getWorld().update(1.f);
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		Assert.assertEquals(a.getPosition().y, 1.0, 0.001);
	}
}
