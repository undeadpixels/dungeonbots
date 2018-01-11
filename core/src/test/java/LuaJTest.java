import com.undead_pixels.dungeon_bots.script.LuaScriptEnvironment;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.undead_pixels.dungeon_bots.script.LuaScript;

import java.util.Optional;


public class LuaJTest {
    private LuaTestClass testClass;

    class LuaTestClass extends ZeroArgFunction {
        int number;

        @Override
        public LuaValue call() {
            number = 100;
            return CoerceJavaToLua.coerce(number);
        }
    }

    @Before
    public void setup() {
        testClass = new LuaTestClass();
    }

    @Test
    public void testLuaJ() {
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
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment();
        LuaScript script = scriptEnv.scriptFromString("x = 1 + 2;");
        Optional<Varargs> results = script.start().join().getResults();
        Assert.assertTrue(
                "ScriptStatus is not marked COMPLETE",
                script.getStatus() == ScriptStatus.COMPLETE);
        Assert.assertTrue(
                "'x = 1 + 2;' does not return the expected result.",
                results.isPresent() && results.get().narg() == 0);

        script = scriptEnv.scriptFromString("return x;");
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
    public void testLuaScriptTimeout() {
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment();
        LuaScript script = scriptEnv.scriptFromString("while true do\nend\n");
        script.start().join(1000);
        Assert.assertTrue(
                "The executed LuaScript should throw an Error after the timeout.",
                script.getStatus() == ScriptStatus.ERROR);
    }

}