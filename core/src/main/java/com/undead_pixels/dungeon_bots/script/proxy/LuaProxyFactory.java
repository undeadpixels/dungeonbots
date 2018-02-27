package com.undead_pixels.dungeon_bots.script.proxy;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.SandboxManager;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.*;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import com.undead_pixels.dungeon_bots.utils.exceptions.MethodNotOnWhitelistException;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Stewart Charles
 * @version 2/1/2018
 * Class containing static methods that generate LuaValues that operate as proxy to invokable Java methods<br>
 * in Lua scripts.
 */
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
	 * Generates a LuaValue that is decorated with LuaFunctions that can invoke
	 * methods of the target object that have been annotated with @Bind
	 * @param src The target object
	 * @param <T> A Type that implements GetLuaFacade
	 * @return A LuaValue that operates as a proxy to the src object
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
	 * Generates a LuaBinding associated with the target objects
	 * that has been decorated with LuaFunctions that can invoke
	 * methods of the target object that have been annotated with @Bind.
	 * @param src The target object
	 * @param <T> A Type that implements GetLuaFacade
	 * @return A LuaBinding to the src object
	 */
	public static <T extends GetLuaFacade> LuaBinding getBindings(final T src) {
		return new LuaBinding(src.getName(), src.getLuaValue());
	}

	/**
	 * Generates a LuaBinding that contains a name and LuaValue that can invoke methods of the target class
	 * @param src The target class
	 * @param <T> A Type that implements GetLuaFacade
	 * @return A LuaBinding to the target object
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
				Optional.ofNullable(src.getDeclaredAnnotation(BindTo.class))
						.map(BindTo::value)
						.orElse(src.getSimpleName()), t);
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

	private static <T extends GetLuaFacade> Varargs invokeWhitelist(final Method m, final Class<?> returnType, final SecurityContext context, final T caller, final Object... args) throws Exception {
		if(context.canExecute(caller, m)) {
			if(returnType.isAssignableFrom(Varargs.class))
				return Varargs.class.cast(m.invoke(caller, args));
			else if(returnType.isAssignableFrom(LuaValue.class))
				return LuaValue.varargsOf(new LuaValue[] { (LuaValue) m.invoke(caller, args)});
			else if(getAllInterfaces(returnType).anyMatch(GetLuaFacade.class::isAssignableFrom))
				return ((GetLuaFacade) m.invoke(caller, args)).getLuaValue();
			else
				return LuaValue.varargsOf(new LuaValue[] {CoerceJavaToLua.coerce(m.invoke(caller, args))});
		} else {
			throw new MethodNotOnWhitelistException(m);
		}
	}

	private static LuaValue[] varargToArr(final Varargs varargs) {
		final int SIZE = varargs.narg();
		final LuaValue[] list = new LuaValue[SIZE];
		for(int i = 1; i < SIZE + 1; i++)
			list[i - 1] = varargs.arg(i);
		return list;
	}

	private static Object[] getParams(final Method m, final Varargs varargs) {
		final Class<?>[] paramTypes = m.getParameterTypes();
		if(paramTypes.length == 1 && paramTypes[0].equals(Varargs.class)) {
			return new Object[] { varargs };
		}
		else if(paramTypes.length == 1 && paramTypes[0].isAssignableFrom(LuaValue[].class))
			return varargToArr(varargs);
		else {
			Object[] ans = varargToArr(varargs);
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
		final Class<?> returnType = m.getReturnType();

		// All Java to LuaBindings are created as VarArgFunction objects
		class Vararg extends VarArgFunction {
			@Override
			public Varargs invoke(Varargs args) {
				try {
					LuaSandbox sandbox = SandboxManager.getCurrentSandbox(); // can be null
					if(sandbox == null) {
						throw new RuntimeException("Sandbox was null");
					}
					return invokeWhitelist(m, returnType, sandbox.getSecurityContext(), caller, getParams(m, args));
				}
				catch (Exception me) {
					me.printStackTrace();
					return LuaValue.error(me.getMessage());
				}
			}
		}
		return CoerceJavaToLua.coerce(new Vararg());
	}

}
