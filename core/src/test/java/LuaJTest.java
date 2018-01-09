import org.junit.Before;
import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;


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
}
