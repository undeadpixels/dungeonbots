package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.lua.*;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.proxy.LuaBinding;
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
        LuaSandbox scriptEnv = new LuaSandbox(SecurityLevel.DEBUG);
        LuaInvocation script = scriptEnv.init("x = 1 + 2;");
        Optional<Varargs> results = script.join().getResults();
        Assert.assertTrue(
                "ScriptStatus is not marked COMPLETE",
                script.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue(
                "'x = 1 + 2;' does not return the expected result.",
                results.isPresent() && results.get().narg() == 0);

        script = scriptEnv.init("return x;");
        results = script.join().getResults();
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
        LuaSandbox scriptEnv = new LuaSandbox(SecurityLevel.DEBUG);
        LuaInvocation script = scriptEnv.enqueueCodeBlock("x = 1 + 2;");
        Assert.assertFalse("Script results should not be present", script.getResults().isPresent());
    }

    @Test
    public void testLuaScriptTimeout() {
        LuaSandbox scriptEnv = new LuaSandbox(SecurityLevel.DEBUG);
        LuaInvocation script = scriptEnv.init("while true do\nend\n");
        script.join(1000);
        Assert.assertTrue(
                "The executed LuaScript should throw an Error after the timeout.",
                script.getStatus() == ScriptStatus.TIMEOUT);
    }

@Test
    public void testCustomGlobals() {
    		// TODO - the globals aren't actually custom
        LuaSandbox scriptEnv = new LuaSandbox();
        Globals globals = scriptEnv.getGlobals();
        LuaTestMethod testMethod = new LuaTestMethod();
        scriptEnv.add(new LuaBinding("addNum", CoerceJavaToLua.coerce(testMethod)));
        List<LuaInvocation> scripts = new ArrayList<>();
        for(int i = 0; i < 100; i++)
            scripts.add(scriptEnv.init(String.format("return addNum(%d);", i + 1)));
        //scripts.forEach(script -> {
        //    Assert.assertTrue(
        //            "ScriptStatus is not set to READY",
        //            script.getStatus() == ScriptStatus.READY);
        //    script.start();
        //});
        for(int i = 0; i < 100; i++) {
            LuaInvocation script = scripts.get(i).join(100);
            Assert.assertEquals(
                    "ScriptStatus is not marked COMPLETE",
                    ScriptStatus.COMPLETE, script.getStatus());
            Assert.assertTrue(
                    "Script did not return the expected result.",
                    script.getResults().isPresent() && script.getResults().get().toint(1) == i);
        }
    }

    @Test
    public void testStopScript() {
        LuaSandbox scriptEnv = new LuaSandbox(SecurityLevel.DEBUG);
        LuaInvocation script = scriptEnv.init("while true do\nend\n");
        script.stop();
        Assert.assertTrue(
                "The executed LuaScript should be stopped.",
                script.getStatus() == ScriptStatus.STOPPED);
    }

    @Test
    public void testLuaError() {
    	//This sandbox is being made of bad Lua.  It should generate an error.
        LuaSandbox scriptEnv = new LuaSandbox(SecurityLevel.DEBUG);
        LuaInvocation script = scriptEnv.init("if = 2");
        script.join();
        Assert.assertTrue(
                "ScriptStatus should report a Lua Error",
                script.getStatus() == ScriptStatus.LUA_ERROR);
        Assert.assertNotNull(script.getError());
    }
}
