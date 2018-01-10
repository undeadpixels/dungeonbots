import com.undead_pixels.dungeon_bots.script.LuaScriptEnvironment;
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

    public class LuaTestClass extends ZeroArgFunction {
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
        Globals globals = JsePlatform.standardGlobals();
        LuaValue test = CoerceJavaToLua.coerce(testClass);
        globals.set("setNum", test);
        LuaValue chunk = globals.load("return setNum();");
        Varargs ans = chunk.invoke();
        org.junit.Assert.assertTrue("", testClass.number == 100);
        org.junit.Assert.assertTrue("", ans.toint(1) == 100);
    }

    @Test
    public void testLuaScriptResult() {
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment();
        LuaScript script = scriptEnv.scriptFromString("x = 1 + 2;");
        Optional<Varargs> results = script.start().join().get().getResults();
        Assert.assertTrue("", results.isPresent() && results.get().narg() == 0);

        script = scriptEnv.scriptFromString("return x;");
        results = script.start().join().get().getResults();
        Assert.assertTrue("", results.isPresent() && results.get().narg() == 1);
        Assert.assertTrue("", results.get().toint(1) == 3);
    }

    @Test
    public void testLuaScriptTimeout() {
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment();
        LuaScript script = scriptEnv.scriptFromString("while true do\nend\n");
        Optional<LuaScript> results = script.start().join(1000);
        Assert.assertTrue("", !results.isPresent());
    }

}