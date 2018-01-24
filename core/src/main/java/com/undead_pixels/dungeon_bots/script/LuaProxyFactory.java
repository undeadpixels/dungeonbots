package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.interfaces.LuaReflection;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;
import com.undead_pixels.dungeon_bots.utils.Exceptions.MethodNotOnWhitelistException;
import com.undead_pixels.dungeon_bots.utils.annotations.*;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class LuaProxyFactory {


	/**
	 * Generates a LuaTable and binds any methods or fields annotated with @BindMethod or BindField
	 * @param securityLevel
	 * @return
	 */
	public static <T extends Scriptable & LuaReflection> LuaBinding getBindings(T src, Whitelist whitelist, final SecurityLevel securityLevel) {
		LuaTable t = new LuaTable();

		/* Use reflection to find and bind any methods annotated using @BindMethod
		 *  that have the appropriate security level */
		src.getBindableMethods(securityLevel)
				.forEach(method ->
						t.set(Optional.ofNullable(method.getDeclaredAnnotation(BindTo.class))
										.map(BindTo::value)
										.orElse(method.getName()),
								evalMethod(src, method, whitelist)));

		/* Use reflection to find and bind any fields annotated using @BindField
		 *  that have the appropriate security level */
		src.getBindableFields(securityLevel)
				.forEach(field -> {
					try {
						field.setAccessible(true);
						t.set(Optional.ofNullable(field.getDeclaredAnnotation(BindTo.class))
										.map(BindTo::value)
										.orElse(field.getName()),
								CoerceJavaToLua.coerce(field.get(src)));
					}
					catch (Exception e) { }
				});
		return new LuaBinding(src.getName(), t);
	}

	@FunctionalInterface
	private interface LuaValueSupplier<T> {
		T get() throws InvocationTargetException, MethodNotOnWhitelistException, IllegalAccessException;
	}

	private static LuaValue filterWhitelist(Method m, Whitelist whitelist, LuaValueSupplier<Object> fn)
			throws MethodNotOnWhitelistException, InvocationTargetException, IllegalAccessException {
		if(whitelist.onWhitelist(m.getName())) {
			return CoerceJavaToLua.coerce(fn.get());
		}
		else
			throw new MethodNotOnWhitelistException(m);
	}

	private static LuaValue evalMethod(final Object caller, final Method m, Whitelist whitelist) {
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
						return filterWhitelist(m, whitelist, () -> m.invoke(caller, args));
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
							return filterWhitelist(m, whitelist,  () -> m.invoke(caller));
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
							return filterWhitelist(m, whitelist, () -> m.invoke(caller, arg));
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
							return filterWhitelist(m, whitelist, () -> m.invoke(caller, arg1, arg2));
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
							return filterWhitelist(m, whitelist, () -> m.invoke(caller, arg1, arg2, arg3));
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
