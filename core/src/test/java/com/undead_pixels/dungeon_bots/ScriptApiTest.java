package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.LuaScriptEnvironment;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.junit.Assert;
import org.junit.Test;
import org.luaj.vm2.Varargs;

public class ScriptApiTest {

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
}
