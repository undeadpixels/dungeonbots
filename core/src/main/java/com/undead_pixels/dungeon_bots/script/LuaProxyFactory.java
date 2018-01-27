package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.*;
import com.undead_pixels.dungeon_bots.utils.Exceptions.MethodNotOnWhitelistException;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;

public class LuaProxyFactory {

	private final static class UpdateError extends ThreeArgFunction {
		@Override
		public final LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
			throw new RuntimeException("Attempt to update readonly table");
		}
	}

	/**
	 *
	 * @param src
	 * @param <T>
	 * @return
	 */
	public static <T extends Scriptable & GetBindable> LuaValue getLuaValue(final T src, final SecurityLevel securityLevel) {
		final LuaTable t = new LuaTable();
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

		// Make Lua proxy table readonly
		LuaTable proxy = new LuaTable();
		LuaTable meta = new LuaTable();
		meta.set("__index", t);
		meta.set("__newindex", CoerceJavaToLua.coerce(new UpdateError()));
		proxy.setmetatable(meta);
		return proxy;
	}


	/**
	 * Generates a LuaTable and binds any methods or fields annotated with @BindMethod or @BindField
	 * @param securityLevel
	 * @return
	 */
	public static <T extends Scriptable & GetBindable> LuaBinding getBindings(final T src, final SecurityLevel securityLevel) {
		return new LuaBinding(src.getName(), getLuaValue(src, securityLevel));
	}

	/**
	 *
	 * @param src
	 * @param securityLevel
	 * @param <T>
	 * @return
	 */
	public static <T extends Scriptable & GetBindable> LuaBinding getBindings(final Class<T> src, final SecurityLevel securityLevel) {
		final LuaTable t = new LuaTable();
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


	private static <T extends Scriptable & GetBindable> Varargs invokeWhitelistVarargs(final Method m, final Whitelist whitelist, final T caller, final Object... args)
			throws MethodNotOnWhitelistException, InvocationTargetException, IllegalAccessException {
		if(whitelist.onWhitelist(caller, m)) {
			return (Varargs) m.invoke(caller, args);
		}
		else
			throw new MethodNotOnWhitelistException(m);
	}


	private static <T extends Scriptable & GetBindable> LuaValue invokeWhitelist(final Method m, final Whitelist whitelist, final T caller, final Object... args)
			throws MethodNotOnWhitelistException, InvocationTargetException, IllegalAccessException {
		if(whitelist.onWhitelist(caller, m)) {
			return CoerceJavaToLua.coerce(m.invoke(caller, args));
		}
		else
			throw new MethodNotOnWhitelistException(m);
	}

	private static LuaValue[] VarargToArr(final Varargs varargs) {
		final int SIZE = varargs.narg();
		LuaValue[] list = new LuaValue[SIZE];
		for(int i = 1; i < SIZE + 1; i++) {
			list[i - 1] = varargs.arg(i);
		}
		return list;
	}

	private static Object[] getParams(final Method m, final Varargs varargs) {
		Class<?>[] paramTypes = m.getParameterTypes();
		if(paramTypes.length > 0 && paramTypes[0].equals(Varargs.class)) {
			return new Object[] { paramTypes.length == varargs.narg() ?
					varargs :
					varargs.subargs(1) };
		}
		else {
			Object[] ans = VarargToArr(varargs);
			if(paramTypes.length == ans.length) {
				return ans;
			}
			else {
				switch (ans.length) {
					case 0:
					case 1:
						return null;
					default:
						return Arrays.copyOfRange(ans, 1, ans.length);
				}
			}
		}
	}

	private static <T extends Scriptable & GetBindable> LuaValue evalMethod(final T caller, final Method m) {
		m.setAccessible(true);
		Class<?> returnType = m.getReturnType();

		// If the expected return type of the function is Varargs or the only parameter is a Varargs treat the function
		// like it is of type VarargFunction
		class Vararg extends VarArgFunction {
			@Override
			public Varargs invoke(Varargs args) {
				try {
					if(returnType.equals(Varargs.class))
						return invokeWhitelistVarargs(m, SecurityContext.activeWhitelist, caller, getParams(m, args));
					else
						return LuaValue.varargsOf(
								new LuaValue[] {
										invokeWhitelist(m, SecurityContext.activeWhitelist, caller, getParams(m, args)) });
				}
				catch (Exception me) {
					return LuaValue.error(me.getMessage());
				}
			}
		}
		return CoerceJavaToLua.coerce(new Vararg());
	}

}
