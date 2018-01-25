package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;
import com.undead_pixels.dungeon_bots.utils.Exceptions.MethodNotOnWhitelistException;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class LuaProxyFactory {

	/**
	 *
	 * @param src
	 * @param <T>
	 * @return
	 */
	public static <T extends Scriptable & GetBindable> LuaValue getLuaValue(T src, SecurityLevel securityLevel) {
		LuaTable t = new LuaTable();
		t.set("this", LuaValue.userdataOf(src));
		/* Use reflection to find and bind any methods annotated using @BindMethod
		 *  that have the appropriate security level */
		src.getBindableMethods(securityLevel)
				.forEach(method ->
						t.set(GetBindable.bindTo(method), evalMethod(src, method)));

		/* Use reflection to find and bind any fields annotated using @BindField
		 *  that have the appropriate security level */
		src.getBindableFields(securityLevel)
				.forEach(field -> {
					try {
						field.setAccessible(true);
						t.set(GetBindable.bindTo(field), CoerceJavaToLua.coerce(field.get(src)));
					}
					catch (Exception e) { }
				});
		return t;
	}


	/**
	 * Generates a LuaTable and binds any methods or fields annotated with @BindMethod or @BindField
	 * @param securityLevel
	 * @return
	 */
	public static <T extends Scriptable & GetBindable> LuaBinding getBindings(T src, final SecurityLevel securityLevel) {
		return new LuaBinding(src.getName(), getLuaValue(src, securityLevel));
	}

	public static <T extends Scriptable & GetBindable> LuaBinding getBindings(Class<T> src, final SecurityLevel securityLevel) {
		LuaTable t = new LuaTable();

		/* Use reflection to find and bind any methods annotated using @BindMethod
		 *  that have the appropriate security level */
		GetBindable.getBindableStaticMethods(src,securityLevel)
				.forEach(method ->
						t.set(GetBindable.bindTo(method), evalMethod(null, method)));

		/* Use reflection to find and bind any fields annotated using @BindField
		 *  that have the appropriate security level */
		GetBindable.getBindableStaticFields(src,securityLevel)
				.forEach(field -> {
					try {
						field.setAccessible(true);
						t.set(GetBindable.bindTo(field), CoerceJavaToLua.coerce(field.get(null)));
					}
					catch (Exception e) { }
				});
		return new LuaBinding(
				Optional.ofNullable(src.getDeclaredAnnotation(BindTo.class)).map(BindTo::value).orElse(src.getSimpleName()),
				t);
	}


	private static <T extends Scriptable & GetBindable> Varargs invokeWhitelistVarargs(Method m, Whitelist whitelist, T caller, Object... args)
			throws MethodNotOnWhitelistException, InvocationTargetException, IllegalAccessException {
		if(whitelist.onWhitelist(caller, m)) {
			return (Varargs) m.invoke(caller, args);
		}
		else
			throw new MethodNotOnWhitelistException(m);
	}


	private static <T extends Scriptable & GetBindable> LuaValue invokeWhitelist(Method m, Whitelist whitelist, T caller, Object... args)
			throws MethodNotOnWhitelistException, InvocationTargetException, IllegalAccessException {
		if(whitelist.onWhitelist(caller, m)) {
			return CoerceJavaToLua.coerce(m.invoke(caller, args));
		}
		else
			throw new MethodNotOnWhitelistException(m);
	}

	private static <T extends Scriptable & GetBindable> LuaValue evalMethod(T caller, final Method m) {
		m.setAccessible(true);
		Class<?>[] paramTypes = m.getParameterTypes();
		Class<?> returnType = m.getReturnType();

		// If the expected return type of the function is Varargs or the only parameter is a Varargs treat the function
		// like it is of type VarargFunction
		if(returnType.equals(Varargs.class) || (paramTypes.length == 1 && paramTypes[0].equals(Varargs.class))) {
			class Vararg extends VarArgFunction {
				@Override
				public Varargs invoke(Varargs args) {
					try {
						return invokeWhitelistVarargs(m, SecurityContext.activeWhitelist, caller, args);
					}
					catch (MethodNotOnWhitelistException me) {
						return LuaValue.error(me.getMessage());
					}
					catch (Exception e) { return LuaValue.NIL; }
				}
			}
			return CoerceJavaToLua.coerce(new Vararg());
		}

		// Otherwise expect 0, 1, 2 or 3 parameters for the method
		switch(paramTypes.length) {
			case 0:
				class ZeroArg extends ZeroArgFunction {
					@Override
					public LuaValue call() {
						try {
							return invokeWhitelist(m, SecurityContext.activeWhitelist, caller);
						}
						catch (MethodNotOnWhitelistException me) {
							return LuaValue.error(me.getMessage());
						}
						catch (Exception e) { return LuaValue.NIL; }
					}
				}
				return CoerceJavaToLua.coerce(new ZeroArg());
			case 1:
				class OneArg extends OneArgFunction {
					@Override
					public LuaValue call(LuaValue arg) {
						try {
							assert Stream.of(paramTypes).allMatch(LuaValue.class::equals);
							return invokeWhitelist(m, SecurityContext.activeWhitelist, caller, arg);
						}
						catch (MethodNotOnWhitelistException me) { return LuaValue.error(me.getMessage()); }
						catch (Exception e) { return LuaValue.NIL; }
					}
				}
				return CoerceJavaToLua.coerce(new OneArg());
			case 2:
				class TwoArg extends TwoArgFunction {
					@Override
					public LuaValue call(LuaValue arg1, LuaValue arg2) {
						try {
							assert Stream.of(paramTypes).allMatch(LuaValue.class::equals);
							return invokeWhitelist(m, SecurityContext.activeWhitelist, caller, arg1, arg2);
						}
						catch (MethodNotOnWhitelistException me) { return LuaValue.error(me.getMessage()); }
						catch (Exception e) { return LuaValue.NIL; }
					}
				}
				return CoerceJavaToLua.coerce(new TwoArg());
			case 3:
				class ThreeArg extends ThreeArgFunction {
					@Override
					public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
						try {
							assert Stream.of(paramTypes).allMatch(LuaValue.class::equals);
							return invokeWhitelist(m, SecurityContext.activeWhitelist, caller, arg1, arg2, arg3);
						}
						catch (MethodNotOnWhitelistException me) { return LuaValue.error(me.getMessage()); }
						catch (Exception e) { return LuaValue.NIL; }
					}
				}
				return CoerceJavaToLua.coerce(new ThreeArg());
			default:
				return LuaValue.NIL;
		}
	}

}
