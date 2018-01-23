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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import com.undead_pixels.dungeon_bots.scene.entities.*;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.utils.annotations.*;
import com.undead_pixels.dungeon_bots.utils.builders.ActorBuilder;
import org.junit.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import static com.undead_pixels.dungeon_bots.TestUtils.cmp;
import static java.lang.String.*;

public class ScriptApiTest {
	
	public ScriptApiTest() {
		System.out.println(new File(".").getAbsolutePath());
	}

	private final double EPSILON = 0.00001;

    @Test
    public void testGetBindings() {
        Actor player = new ActorBuilder().setName("player").createActor();
        LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript luaScript = se.script("player.up();");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue(cmp(player.getPosition().y, 1.0, EPSILON));
    }

    @Test
    public void testScriptApiSingleArgumentFunction() {
		class OneArg extends Entity {

			OneArg(String name) {
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

			@ScriptAPI(SecurityLevel.AUTHOR) @BindTo("greeting")
			public LuaValue greet(LuaValue luaValue) {
				String greet = luaValue.checkjstring();
				return CoerceJavaToLua.coerce(greet + " " + this.name);
			}
		}

		OneArg player = new OneArg( "player");
		LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);
		LuaScript luaScript = se.script("return player.greeting('Hello');");
		luaScript.start().join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE
				&& luaScript.getResults().isPresent());
		Varargs ans = luaScript.getResults().get();
		Assert.assertTrue(ans.tojstring(1).equals("Hello player"));
    }

    @Test
    public void testSecurityLevel() {
		class DebugError extends Entity {

			DebugError(String name) {
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

			// Tag the error function with a 'high' security level
			@ScriptAPI(SecurityLevel.DEBUG)
			public LuaValue error() {
				return LuaValue.NIL;
			}
		}

        DebugError player = new DebugError( "player");
		// Create a LuaScriptEnvironment with a SecurityLevel less than what the error function is tagged with
        LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.AUTHOR);
        LuaScript luaScript = se.script("return player.error();");
        luaScript.start().join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.LUA_ERROR);
    }

    @Test public void testActorMovement() {
        Actor player = new ActorBuilder().setName("player").createActor();
        LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);

        LuaScript luaScript = se.init("player.up();").join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue("Player Y Position not moved 'UP'",
                cmp(player.getPosition().y, 1.0, EPSILON));

        luaScript = se.init("player.down();").join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue("Player Y Position not moved 'DOWN'",
                cmp(player.getPosition().y, 0.0, EPSILON));

        luaScript = se.init("player.left();").join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue("Player X Position not moved 'LEFT'",
                cmp(player.getPosition().x, -1.0, EPSILON));

        luaScript = se.init("player.right();").join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue("Player X Position not moved 'RIGHT'",
                cmp(player.getPosition().x, 0.0, EPSILON));
    }

	@Test public void testActorPosition() {
		Actor player = new ActorBuilder().setName("player").createActor();
		LuaScriptEnvironment se = player.getScriptEnvironment(SecurityLevel.DEBUG);

		LuaScript luaScript = se.script("return player.position();");
		luaScript.start().join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		Assert.assertTrue(luaScript.getResults().isPresent());
		Varargs ans = luaScript.getResults().get();
		Assert.assertTrue(cmp(ans.tofloat(1), ans.tofloat(2), EPSILON));
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
    	Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
    	int ans = luaScript.getResults().get().toint(1);
    	Assert.assertTrue(ans == 38);
    	Assert.assertTrue(testEntity.number == 38);
	}

	@Test
	public void testThreeArgFunction() {
		class TestEntity extends Entity {

			private int number = 0;

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
		Assert.assertTrue("Initial value of Entity number is not expected value",
				testEntity.number == 0);
		LuaScript luaScript = scriptEnvironment.init("return test.add(7, 23, 45);").join();
		Assert.assertTrue("Expected ScriptStatus of COMPLETE",
				luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
		int ans = luaScript.getResults().get().toint(1);
		Assert.assertTrue(format("Actual return value '%d' does not match expected '75'", ans),
				ans == 75);
		Assert.assertTrue(format("Actual Entity number '%d does not match expected '75'", testEntity.number),
				testEntity.number == 75);
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
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
		int ans = luaScript.getResults().get().toint(1);
		Assert.assertTrue(format("Expected result of script: '36' does not equal actual: '%d'", ans),
				ans == 36);
		Assert.assertTrue(format("Expected entity field value: '36' does not match actual: '%d'", testEntity.number),
				testEntity.number == 36);
	}

	@Test public void testBindField() {
		class RpgActor extends Entity {

			@BindField
			private Integer strength;

			@BindField
			private Integer dexterity;

			@BindField
			private Integer intelligence;

			public RpgActor(String name, int str, int dex ,int intel) {
				super(new World(), name);
				this.strength = str;
				this.dexterity = dex;
				this.intelligence = intel;
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
		}

		RpgActor rpg = new RpgActor("rpg",4 , 5, 6);
		LuaScriptEnvironment se = rpg.getScriptEnvironment(SecurityLevel.DEBUG);
		LuaScript luaScript = se.init("return rpg.strength, rpg.dexterity, rpg.intelligence;").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
		Varargs v = luaScript.getResults().get();
		Assert.assertTrue(v.toint(1) == 4 && v.toint(2) == 5 && v.toint(3) == 6);
	}

	@Test public void testModifyField() {
		class RpgActor extends Entity {

			@BindField
			public final LuaTable stats;

			public RpgActor(String name, int str, int dex ,int intel) {
				super(new World(), name);
				stats = new LuaTable();
				stats.set("strength", str);
				stats.set("dexterity", dex);
				stats.set("intelligence", intel);
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
		}

		RpgActor rpgEntity = new RpgActor("rpg",4 , 5, 6);
		LuaScriptEnvironment se = rpgEntity.getScriptEnvironment(SecurityLevel.DEBUG);
		LuaScript luaScript = se.init("return rpg.stats.strength, rpg.stats.dexterity, rpg.stats.intelligence;").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
		Varargs v = luaScript.getResults().get();
		Assert.assertTrue(v.toint(1) == 4 && v.toint(2) == 5 && v.toint(3) == 6);

		Assert.assertTrue(rpgEntity.stats.get("strength").toint() == 4);
		Assert.assertTrue(rpgEntity.stats.get("dexterity").toint() == 5);
		Assert.assertTrue(rpgEntity.stats.get("intelligence").toint() == 6);

		se.init("rpg.stats.strength = rpg.stats.strength - 1").join();
		se.init("rpg.stats.dexterity = rpg.stats.dexterity - 1").join();
		se.init("rpg.stats.intelligence = rpg.stats.intelligence - 1").join();

		Assert.assertTrue(rpgEntity.stats.get("strength").toint() == 3);
		Assert.assertTrue(rpgEntity.stats.get("dexterity").toint() == 4);
		Assert.assertTrue(rpgEntity.stats.get("intelligence").toint() == 5);
	}

}
