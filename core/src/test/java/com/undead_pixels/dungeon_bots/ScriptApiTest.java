package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.*;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.interfaces.*;
import org.junit.*;
import org.luaj.vm2.*;
import com.undead_pixels.dungeon_bots.utils.builders.ActorBuilder;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import static java.lang.String.*;

public class ScriptApiTest {

	private final double EPSILON = 0.00001;

    @Test public void testGetBindings() {
        Player player = new Player(new World(), "player");
        LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG);
        se.addBindable(player);
        LuaInvocation luaScript = se.init("player:up();");
        luaScript.join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		player.getWorld().setSize(16,16);
		player.getWorld().update(1.f);
        Assert.assertEquals( 1.0, player.getPosition().y, EPSILON);
    }

    @Test public void testScriptApiSingleArgumentFunction() {
		class OneArg implements GetLuaFacade {

			String name;

			OneArg(String name) {
				this.name = name;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getId() {
				return 0;
			}

			@Bind(SecurityLevel.AUTHOR) @BindTo("greeting")
			public LuaValue greet(LuaValue luaValue) {
				String greet = luaValue.checkjstring();
				return CoerceJavaToLua.coerce(greet + " " + this.name);
			}
		}

		OneArg player = new OneArg( "player");
		LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG).addBindable(player);
		LuaInvocation luaScript = se.init("return player:greeting('Hello');");
		luaScript.join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE
				&& luaScript.getResults().isPresent());
		Varargs ans = luaScript.getResults().get();
		Assert.assertTrue(ans.tojstring(1).equals("Hello player"));
    }

    @Test public void testSecurityLevel() {
		class DebugError implements GetLuaFacade {

			String name;

			DebugError(String name) {
				this.name = name;
			}

			// Tag the error function with a 'high' security level
			@Bind(SecurityLevel.DEBUG)
			public LuaValue error() {
				return LuaValue.NIL;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getId() {
				return 0;
			}
		}

        DebugError player = new DebugError( "player");
		// Create a LuaSandbox with a SecurityLevel less than what the error function is tagged with
        LuaSandbox se = new LuaSandbox(SecurityLevel.AUTHOR).addBindable(player);
        LuaInvocation luaScript = se.init("return player:error();");
        luaScript.join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.LUA_ERROR);
    }

    @Test public void testActorMovement() {
        Actor player = new ActorBuilder().setName("player").createActor();
        World w = player.getWorld();
        w.setSize(16,16);
        LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG).addBindable(player);

        LuaInvocation luaScript = se.init("player:up();").join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
        w.update(1.f);
        Assert.assertEquals("Player Y Position not moved 'UP'",
                 1.0, player.getPosition().y, EPSILON);

        luaScript = se.init("player:down();").join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		w.update(1.f);
        Assert.assertEquals("Player Y Position not moved 'DOWN'",
				0.0, player.getPosition().y, EPSILON);

		luaScript = se.init("player:right();").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		w.update(1.f);
		Assert.assertEquals("Player X Position not moved 'RIGHT'",
				1.0, player.getPosition().x, EPSILON);

        luaScript = se.init("player:left();").join();
        Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		w.update(1.f);
        Assert.assertEquals("Player X Position not moved 'LEFT'",
				0.0, player.getPosition().x, EPSILON);

    }

	@Test public void testActorPosition() {
		Actor player = new ActorBuilder().setName("player").createActor();
		LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG).addBindable(player);

		LuaInvocation luaScript = se.init("return player.position();");
		luaScript.join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE);
		Assert.assertTrue(luaScript.getResults().isPresent());
		Varargs ans = luaScript.getResults().get();
		Assert.assertEquals(1.0, ans.arg(1).todouble(), EPSILON);
		Assert.assertEquals(1.0, ans.arg(2).todouble(), EPSILON);
	}

    @Test public void testTwoArgFunction() {
    	class TestEntity implements GetLuaFacade {

    		int number = 0;
			String name;

    		TestEntity(String name) {
    			this.name = name;
			}

			@Bind @BindTo("add")
			public LuaValue setValues(LuaValue a, LuaValue b) {
    			number = a.checkint() + b.checkint();
    			return CoerceJavaToLua.coerce(number);
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getId() {
				return 0;
			}
		}

		TestEntity testEntity = new TestEntity("test");
    	LuaSandbox scriptEnvironment = new LuaSandbox(SecurityLevel.DEBUG).addBindable(testEntity);
    	LuaInvocation luaScript = scriptEnvironment.init("return test:add(15,23);").join();
    	Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
    	int ans = luaScript.getResults().get().toint(1);
    	Assert.assertTrue(ans == 38);
    	Assert.assertTrue(testEntity.number == 38);
	}

	@Test public void testThreeArgFunction() {
		class TestEntity implements GetLuaFacade {

			String name;
			private int number = 0;

			TestEntity(String name) {
				this.name = name;
			}

			@Bind @BindTo("add")
			public LuaValue setValues(LuaValue a, LuaValue b, LuaValue c) {
				number = a.checkint() + b.checkint() + c.checkint();
				return CoerceJavaToLua.coerce(number);
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getId() {
				return 0;
			}
		}

		TestEntity testEntity = new TestEntity("test");
		LuaSandbox scriptEnvironment = new LuaSandbox(SecurityLevel.DEBUG).addBindable(testEntity);
		Assert.assertTrue("Initial value of Entity number is not expected value",
				testEntity.number == 0);
		LuaInvocation luaScript = scriptEnvironment.init("return test:add(7, 23, 45);").join();
		Assert.assertTrue("Expected ScriptStatus of COMPLETE",
				luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
		int ans = luaScript.getResults().get().toint(1);
		Assert.assertTrue(format("Actual return value '%d' does not match expected '75'", ans),
				ans == 75);
		Assert.assertTrue(format("Actual Entity number '%d does not match expected '75'", testEntity.number),
				testEntity.number == 75);
	}

	@Test public void testVarArgsFunction() {
		class TestEntity implements GetLuaFacade {

			String name;
			int number = 0;

			TestEntity(String name) {
				this.name = name;
			}

			@Bind @BindTo("add")
			public Varargs addValues(Varargs v) {
				int num = v.narg();
				int ans = 0;
				for(int i = 0; i < num; i++) {
					ans += v.toint(i + 1);
				}
				number = ans;
				return CoerceJavaToLua.coerce(ans);
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getId() {
				return 0;
			}
		}

		TestEntity testEntity = new TestEntity("test");
		LuaSandbox scriptEnvironment = new LuaSandbox(SecurityLevel.DEBUG).addBindable(testEntity);
		LuaInvocation luaScript = scriptEnvironment.init("return test.add(1,2,3,4,5,6,7,8);").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
		int ans = luaScript.getResults().get().toint(1);
		Assert.assertTrue(format("Expected result of sandbox: '36' does not equal actual: '%d'", ans),
				ans == 36);
		Assert.assertTrue(format("Expected entity field value: '36' does not match actual: '%d'", testEntity.number),
				testEntity.number == 36);
	}

	@Test public void testBindField() {
		class RpgActor implements GetLuaFacade {

			String name;

			@Bind
			private Integer strength;

			@Bind
			private Integer dexterity;

			@Bind
			private Integer intelligence;

			public RpgActor(String name, int str, int dex ,int intel) {
				this.name = name;
				this.strength = str;
				this.dexterity = dex;
				this.intelligence = intel;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getId() {
				return 0;
			}
		}

		RpgActor rpg = new RpgActor("rpg",4 , 5, 6);
		LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG).addBindable(rpg);
		LuaInvocation luaScript = se.init("return rpg.strength, rpg.dexterity, rpg.intelligence;").join();
		Assert.assertTrue(luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent());
		Varargs v = luaScript.getResults().get();
		Assert.assertTrue(v.toint(1) == 4 && v.toint(2) == 5 && v.toint(3) == 6);
	}

	@Test public void testModifyField() {
		class RpgActor implements GetLuaFacade {

			String name;

			@Bind
			public final LuaTable stats;

			public RpgActor(String name, int str, int dex ,int intel) {
				this.name = name;
				stats = new LuaTable();
				stats.set("strength", str);
				stats.set("dexterity", dex);
				stats.set("intelligence", intel);
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getId() {
				return 0;
			}
		}

		RpgActor rpgEntity = new RpgActor("rpg",4 , 5, 6);
		LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG).addBindable(rpgEntity);
		LuaInvocation luaScript = se.init("return rpg.stats.strength, rpg.stats.dexterity, rpg.stats.intelligence;").join();
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

	@Test public void testStaticMethods() {
    	LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG)
				.addBindableClass(Player.class)
				.addBindableClass(World.class);
    	LuaInvocation script = se.init("w = World.new(); return Player.new(w,1.0,1.0);").join();
    	Assert.assertTrue(script.getStatus() == ScriptStatus.COMPLETE && script.getResults().isPresent());
	}

	@Test public void testGetUserData() {
		LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG)
				.addBindable(new World("w"))
				.addBindableClass(Player.class);
		LuaInvocation script = se.init("return Player.new(w,2.0,2.0);").join();
		Assert.assertTrue(script.getStatus() == ScriptStatus.COMPLETE && script.getResults().isPresent());
		LuaTable t = script.getResults().get().arg(1).checktable();
		Player p = (Player) t.get("this").checkuserdata(Player.class);
		Assert.assertTrue(p != null);
		Assert.assertEquals(p.getPosition().x, 1.0, 0.001);
		Assert.assertEquals(p.getPosition().y, 1.0, 0.001);
	}

	@Test
	public void testGetAndUseUserData() {
    	World w = new World("world");
		LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG)
				.addBindable(w)
				.addBindableClass(Player.class);

		LuaInvocation script = se.init("p = Player.new(world,2.0,2.0); return p;").join();
		Assert.assertTrue(script.getStatus() == ScriptStatus.COMPLETE && script.getResults().isPresent());
		LuaTable t = script.getResults().get().arg(1).checktable();
		Player p = (Player) t.get("this").checkuserdata(Player.class);
		Assert.assertTrue(p != null);
		Assert.assertEquals( 1.0, p.getPosition().y,0.001);
		Assert.assertEquals( 1.0, p.getPosition().y, 0.001);
		Assert.assertTrue(script.getStatus() == ScriptStatus.COMPLETE);
		script = se.init("return p:position();").join();
		Assert.assertTrue(script.getStatus() == ScriptStatus.COMPLETE && script.getResults().isPresent());
		Varargs ans = script.getResults().get();
		Assert.assertEquals(2.0, ans.arg(1).todouble(), EPSILON);
		Assert.assertEquals(2.0, ans.arg(2).todouble(), EPSILON);
	}

	@Test
	public void testReadOnlyTables() {
		World world = new World("w");
		LuaSandbox se = new LuaSandbox(SecurityLevel.DEBUG)
				.addBindable(world)
				.addBindableClass(Player.class);

		LuaInvocation script = se.init("p = Player.new(w,1.0,1.0); p.this = nil; return p;").join();
		//Assert.assertEquals(ScriptStatus.LUA_ERROR, script.getStatus());
		Assert.assertTrue(script.getError().getMessage().contains("Attempt to update readonly table"));
	}
}
