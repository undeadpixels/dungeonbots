package com.undead_pixels.dungeon_bots.script.proxy;

import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.*;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import com.undead_pixels.dungeon_bots.utils.Exceptions.MethodNotOnWhitelistException;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

public class LuaProxyFactory {

	/**
	 * Create Handler function for proxy metatable to throw exceptions when users try to SET
	 * values in READONLY table
	 */
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
	public static <T extends GetLuaFacade> LuaValue getLuaValue(final T src) {
		final LuaTable t = new LuaTable();
		t.set("this", LuaValue.userdataOf(src));
		/* Use reflection to find and bind any methods annotated using @Bind
		 *  that have the appropriate security level */
		src.getBindableMethods()
				.forEach(method ->
						t.set(GetLuaFacade.bindTo(method), evalMethod(src, method)));

		/* Use reflection to find and bind any fields annotated using @Bind
		 * that have the appropriate security level */
		src.getBindableFields()
				.forEach(field -> {
					try {
						field.setAccessible(true);
						t.set(GetLuaFacade.bindTo(field), CoerceJavaToLua.coerce(field.get(src)));
					}
					catch (Exception e) { }
				});

		// Make Lua proxy table readonly
		final LuaTable proxy = new LuaTable();
		final LuaTable meta = new LuaTable();
		meta.set("__index", t);
		meta.set("__newindex", CoerceJavaToLua.coerce(new UpdateError()));
		proxy.setmetatable(meta);
		return proxy;
	}


	/**
	 * Generates a LuaTable and binds any methods or fields annotated with @BindMethod or @BindField
	 * @return
	 */
	public static <T extends GetLuaFacade> LuaBinding getBindings(final T src) {
		return new LuaBinding(src.getName(), src.getLuaValue());
	}

	/**
	 *
	 * @param src
	 * @param <T>
	 * @return
	 */
	public static <T extends GetLuaFacade> LuaBinding getBindings(final Class<T> src) {
		final LuaTable t = new LuaTable();
		/* Use reflection to find and bind any methods annotated using @BindMethod
		 *  that have the appropriate security level */
		GetLuaFacade.getBindableStaticMethods(src)
				.forEach(method ->
						t.set(GetLuaFacade.bindTo(method), evalMethod(null, method)));

		/* Use reflection to find and bind any fields annotated using @BindField
		 *  that have the appropriate security level */
		GetLuaFacade.getBindableStaticFields(src)
				.forEach(field -> {
					try {
						field.setAccessible(true);
						t.set(GetLuaFacade.bindTo(field), CoerceJavaToLua.coerce(field.get(null)));
					}
					catch (Exception e) { }
				});
		return new LuaBinding(
				Optional.ofNullable(src.getDeclaredAnnotation(BindTo.class)).map(BindTo::value).orElse(src.getSimpleName()),
				t);
	}

	private static Stream<Class<?>> getAllInterfaces(final Class<?> c) {
		Set<Class<?>> clzSet = new HashSet<>();
		Class<?> s = c;
		try {
			while(!Object.class.equals(s.getClass())) {
				clzSet.addAll(Arrays.asList(s.getInterfaces()));
				s = s.getSuperclass();
			}
		}
		catch (NullPointerException n) { }
		return clzSet.stream();
	}

	private static <T extends GetLuaFacade> Varargs invokeWhitelist(final Method m, final Class<?> returnType, final Whitelist whitelist, final T caller, final Object... args)
			throws MethodNotOnWhitelistException, InvocationTargetException, IllegalAccessException {
		if(whitelist.onWhitelist(caller, m)) {
			if(returnType.isAssignableFrom(Varargs.class))
				return (Varargs) m.invoke(caller, args);
			else if(returnType.isAssignableFrom(LuaValue.class)) {
				return LuaValue.varargsOf(new LuaValue[] { (LuaValue) m.invoke(caller, args)});
			}
			else {
				if(getAllInterfaces(returnType).anyMatch(GetLuaFacade.class::isAssignableFrom))
					return ((GetLuaFacade) m.invoke(caller, args)).getLuaValue();
				else
					return LuaValue.varargsOf(new LuaValue[] {CoerceJavaToLua.coerce(m.invoke(caller, args))});
			}

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
			return new Object[] {
					paramTypes.length == varargs.narg() ?
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

	private static <T extends GetLuaFacade> LuaValue evalMethod(final T caller, final Method m) {
		m.setAccessible(true);
		Class<?> returnType = m.getReturnType();

		// All Java to LuaBindings are created as VarArgFunction objects
		class Vararg extends VarArgFunction {
			@Override
			public Varargs invoke(Varargs args) {
				try {
					return invokeWhitelist(m, returnType, SecurityContext.getWhitelist(), caller, getParams(m, args));
				}
				catch (Exception me) {
					return LuaValue.error(me.getMessage());
				}
			}
		}
		return CoerceJavaToLua.coerce(new Vararg());
	}

}
