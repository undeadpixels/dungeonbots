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


public class LuaJTest {
    private LuaValue _G;
    private LuaTestClass testClass;

    public class LuaTestClass extends ZeroArgFunction {
        int number;

        @Override
        public LuaValue call() {
            number = 100;
            return null;
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
        LuaValue chunk = globals.load("setNum()");
        chunk.call();
        org.junit.Assert.assertTrue("", testClass.number == 100);
    }

    @Test
    public void testLuaExecutor() {
        LuaScriptEnvironment scriptEnv = new LuaScriptEnvironment();
        LuaScript scr = scriptEnv.fromString("x = 1 + 2");
        Varargs results = scr.start().join().getResults();
        Assert.assertTrue("", results.narg() == 0);

        scr = scriptEnv.fromString("return x");
        results = scr.start().join().getResults();
        Assert.assertTrue("", results.narg() == 1);
        Assert.assertTrue("", results.toint(0) == 3);
    }
}
