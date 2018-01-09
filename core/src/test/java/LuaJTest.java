import org.junit.Before;
import org.junit.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;


public class LuaJTest {
    private LuaValue _G;
    private LuaTestClass testClass;

    public class LuaTestClass extends ZeroArgFunction {
        int number;
        public void setNumber() {
            number = 100;
        }

        @Override
        public LuaValue call() {
            setNumber();
            return null;
        }
    }

    @Before
    public void setup() {
        _G = JsePlatform.standardGlobals();
        testClass = new LuaTestClass();
    }

    @Test
    public void testLuaJ() {
        /*
        testClass.number = 0;
        LuaValue test = CoerceJavaToLua.coerce(testClass);
        _G.set("setNumber", testClass);
        LuaValue chunk = _G.load("setNumber()");
        chunk.call();
        System.out.print(testClass.number);
        org.junit.Assert.assertTrue(
                "Expected call to obj:setNumber to set number to 100",
                testClass.number == 100);
                */
    }
}
