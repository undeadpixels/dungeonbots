package com.undead_pixels.dungeon_bots;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.LuaScriptEnvironment;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;
import com.undead_pixels.dungeon_bots.utils.annotations.BindTo;
import com.undead_pixels.dungeon_bots.utils.annotations.ScriptAPI;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.junit.Assert;
import org.junit.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class ScriptApiTest {

	private boolean cmp(double a, double b, double epsilon) {
		return Math.abs(a - b) < epsilon;
	}

    @Test
    public void testGetBindings() {
        Actor player = new Actor(new World(), "player", null);
        LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript luaScript = se.script("player.up();");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue(Math.abs(player.getPosition().y + 1.0) < 0.01);
    }

    @Test
    public void testScriptApiSingleArgumentFunction() {
        Actor player = new Actor(new World(), "player", null);
        LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript luaScript = se.script("return player.greet('Hello');");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Varargs ans = luaScript.getResults().get();
        Assert.assertTrue(ans.tojstring(1).equals("Hello player"));
    }

    @Test
    public void testScriptApiSecurityLevel() {
        Actor player = new Actor(new World(), "player", null);
        LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.AUTHOR);
        LuaScript luaScript = se.script("return player.greet('Hello');");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.LUA_ERROR);
    }

    @Test public void testActorMovement() {
        Actor player = new Actor(new World(), "player", null);
        LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);

        LuaScript luaScript = se.script("player.up();");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue("Player Y Position not moved 'UP'",
                Math.abs(player.getPosition().y + 1.0) < 0.01);

        luaScript = se.script("player.down();");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue("Player Y Position not moved 'DOWN'",
                Math.abs(player.getPosition().y) < 0.01);

        luaScript = se.script("player.left();");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue("Player X Position not moved 'LEFT'",
                Math.abs(player.getPosition().x + 1.0) < 0.01);

        luaScript = se.script("player.right();");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue("Player X Position not moved 'RIGHT'",
                Math.abs(player.getPosition().x) < 0.01);
    }

	@Test public void testActorPosition() {
		Actor player = new Actor(new World(), "player", null);
		LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);

		LuaScript luaScript = se.script("return player.position();");
		luaScript.start().join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		Assert.assertTrue(luaScript.getResults().isPresent());
		Varargs ans = luaScript.getResults().get();
		Assert.assertTrue(cmp(ans.tofloat(1), ans.tofloat(2), 0.01f));
	}

    @Test
	public void testTwoArgFunction() {
    	class TestEntity extends Entity {

    		public int number = 0;

    		TestEntity(String name) {
				super(new World(), name);
			}

			@Override
			public Vector2 getPosition() {
				return null;
			}

			@Override
			public boolean isSolid() {
				return false;
			}

			@Override
			public void update(float dt) {

			}

			@Override
			public void render(SpriteBatch batch) {

			}

			@Override
			public float getZ() {
				return 0;
			}

			@ScriptAPI @BindTo("add")
			public LuaValue setValues(LuaValue a, LuaValue b) {
    			number = a.checkint() + b.checkint();
    			return CoerceJavaToLua.coerce(number);
			}
		}

		TestEntity testEntity = new TestEntity("test");
    	LuaScriptEnvironment scriptEnvironment = testEntity.getScriptEnvironment();
    	LuaScript luaScript = scriptEnvironment.init("return test.add(15,23);").join();
    	Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
    	int ans = luaScript.getResults().get().toint(1);
    	Assert.assertTrue(ans == 38);
    	Assert.assertTrue(testEntity.number == 38);
	}

	@Test
	public void testThreeArgFunction() {
		class TestEntity extends Entity {

			public int number = 0;

			TestEntity(String name) {
				super(new World(), name);
			}

			@Override
			public Vector2 getPosition() {
				return null;
			}

			@Override
			public boolean isSolid() {
				return false;
			}

			@Override
			public void update(float dt) {

			}

			@Override
			public void render(SpriteBatch batch) {

			}

			@Override
			public float getZ() {
				return 0;
			}

			@ScriptAPI @BindTo("add")
			public LuaValue setValues(LuaValue a, LuaValue b, LuaValue c) {
				number = a.checkint() + b.checkint() + c.checkint();
				return CoerceJavaToLua.coerce(number);
			}
		}

		TestEntity testEntity = new TestEntity("test");
		LuaScriptEnvironment scriptEnvironment = testEntity.getScriptEnvironment();
		LuaScript luaScript = scriptEnvironment.init("return test.add(7, 23, 45);").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		int ans = luaScript.getResults().get().toint(1);
		Assert.assertTrue(ans == 75);
		Assert.assertTrue(testEntity.number == 75);
	}

	@Test
	public void testVarArgsFunction() {
		class TestEntity extends Entity {

			public int number = 0;

			TestEntity(String name) {
				super(new World(), name);
			}

			@Override
			public Vector2 getPosition() {
				return null;
			}

			@Override
			public boolean isSolid() {
				return false;
			}

			@Override
			public void update(float dt) {

			}

			@Override
			public void render(SpriteBatch batch) {

			}

			@Override
			public float getZ() {
				return 0;
			}

			@ScriptAPI @BindTo("add")
			public Varargs addValues(Varargs v) {
				int num = v.narg();
				int ans = 0;
				for(int i = 0; i < num; i++) {
					ans += v.toint(i + 1);
				}
				number = ans;
				return CoerceJavaToLua.coerce(ans);
			}
		}

		TestEntity testEntity = new TestEntity("test");
		LuaScriptEnvironment scriptEnvironment = testEntity.getScriptEnvironment();
		LuaScript luaScript = scriptEnvironment.init("return test.add(1,2,3,4,5,6,7,8);").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		int ans = luaScript.getResults().get().toint(1);
		Assert.assertTrue(ans == 36);
		Assert.assertTrue(testEntity.number == 36);
	}
}
