package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.lua.*;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.junit.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;
import java.util.*;

public class LuaJTest {

    @Test
    public void testLuaJ() {
        LuaTestClass testClass = new LuaTestClass();
        testClass.number = 0;
        Globals globals = JsePlatform.standardGlobals();
        LuaValue test = CoerceJavaToLua.coerce(testClass);
        globals.set("setNum", test);
        LuaValue chunk = globals.load("return setNum();");
        Varargs ans = chunk.invoke();
        org.junit.Assert.assertTrue(
                "Calling setNum does not set the value of the Java object to the correct value",
                testClass.number == 100);
        org.junit.Assert.assertTrue(
                "Calling setNum does not return the correct value in Lua",
                ans.toint(1) == 100);
    }

    @Test
    public void testLuaScriptResult() {
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript script = scriptEnv.script("x = 1 + 2;");
        Optional<Varargs> results = script.start().join().getResults();
        Assert.assertTrue(
                "ScriptStatus is not marked COMPLETE",
                script.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue(
                "'x = 1 + 2;' does not return the expected result.",
                results.isPresent() && results.get().narg() == 0);

        script = scriptEnv.script("return x;");
        results = script.start().join().getResults();
        Assert.assertTrue(
                "ScriptStatus is not marked COMPLETE",
                script.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue(
                "'return x;' does not return the expected number of results",
                results.isPresent() && results.get().narg() == 1);
        Assert.assertTrue(
                "'x = 1 + 2;' does not return '3'",
                results.get().toint(1) == 3);
    }

    
    @Test
    public void testNoResults() {
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript script = scriptEnv.script("x = 1 + 2;");
        Assert.assertFalse("Script results should not be present", script.getResults().isPresent());
    }

    @Test
    public void testLuaScriptTimeout() {
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript script = scriptEnv.script("while true do\nend\n");
        script.start().join(1000);
        Assert.assertTrue(
                "The executed LuaScript should throw an Error after the timeout.",
                script.getStatus() == ScriptStatus.TIMEOUT);
    }

    @Test
    public void testCustomGlobals() {
        Globals globals = JsePlatform.standardGlobals();
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment(globals);
        LuaTestMethod testMethod = new LuaTestMethod();
        scriptEnv.add(new LuaBinding("addNum", CoerceJavaToLua.coerce(testMethod)));
        List<LuaScript> scripts = new ArrayList<>();
        for(int i = 0; i < 100; i++)
            scripts.add(scriptEnv.script(String.format("return addNum(%d);", i + 1)));
        scripts.forEach(script -> {
            Assert.assertTrue(
                    "ScriptStatus is not set to READY",
                    script.getStatus() == ScriptStatus.READY);
            script.start();
        });
        for(int i = 0; i < 100; i++) {
            LuaScript script = scripts.get(i).join();
            Assert.assertTrue(
                    "ScriptStatus is not marked COMPLETE",
                    script.getStatus() == ScriptStatus.COMPLETE);
            Assert.assertTrue(
                    "Script did not return the expected result.",
                    script.getResults().isPresent() && script.getResults().get().toint(1) == i);
        }
    }

    @Test
    public void testStopScript() {
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript script = scriptEnv.script("while true do\nend\n");
        script.start().stop();
        Assert.assertTrue(
                "The executed LuaScript should be stopped.",
                script.getStatus() == ScriptStatus.STOPPED);
    }

    @Test
    public void testLuaError() {
    	//This script is being made of bad Lua.  It should generate an error.
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment(SecurityLevel.DEBUG);
        LuaScript script = scriptEnv.script("if = 2");
        script.start().join();
        Assert.assertTrue(
                "ScriptStatus should report a Lua Error",
                script.getStatus() == ScriptStatus.LUA_ERROR);
        Assert.assertNotNull(script.getError());
    }
}