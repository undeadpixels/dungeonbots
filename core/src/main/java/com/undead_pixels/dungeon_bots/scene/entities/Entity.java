package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.utils.Exceptions.MethodNotOnWhitelistException;
import com.undead_pixels.dungeon_bots.utils.annotations.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Pretty much everything visible/usable within a regular game. Does not include UI elements.
 */
public abstract class Entity implements BatchRenderable {

	private Whitelist whitelist;

	/**
	 * A user script that is run on this object
	 */
	protected LuaScript script;
	
	/**
	 * The world of which this Entity is a part
	 */
	protected final World world;
	
	/**
	 * Some simple int that can uniquely identify this entity
	 */
	protected final int id;
	
	/**
	 * A name for this entity that can potentially be user-facing
	 */
	protected final String name;

	/**
	 * @param world		The world to contain this Actor
	 */
	public Entity(World world, String name) {
		this(world, name, null);
		world.addEntity(this);
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user script that is run on this object
	 */
	public Entity(World world, String name, LuaScript script) {
		super();
		this.world = world;
		this.script = script;
		this.name = name;
		this.id = world.makeID();
		this.whitelist = this.defaultWhitelist();
	}

	public Entity(World world, String name, LuaScript luaScript, Whitelist whitelist) {
		this(world,name,luaScript);
		this.whitelist = whitelist;
	}

	/**
	 * @return		The user script
	 */
	public LuaScript getScript() {
		return script;
	}
	
	/**
	 * @param script		The user script to set
	 */
	public void setScript(LuaScript script) {
		this.script = script;
	}
	
	/**
	 * @return		This Entity's position in tile space
	 */
	public abstract Vector2 getPosition();
	
	
	/**
	 * @return		If this object disallows movement through it
	 */
	public abstract boolean isSolid();

	/**
	 * Generates a LuaScriptEnvironment for the given entity
	 * @param securityLevel The Security level of the requested LuaScriptEnvironment
	 * @return
	 */
	public final LuaScriptEnvironment getScriptEnvironment(SecurityLevel securityLevel) {
	    LuaScriptEnvironment scriptEnvironment = new LuaScriptEnvironment(securityLevel);
	    scriptEnvironment.add(getBindings(securityLevel));
	    return scriptEnvironment;
    }

	/**
	 * Generates a LuaScriptEnvironment for the given entity
	 * @return
	 */
	public final LuaScriptEnvironment getScriptEnvironment() {
		return getScriptEnvironment(SecurityLevel.AUTHOR);
	}

	public Whitelist getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(Whitelist whitelist) {
		this.whitelist = whitelist;
	}

	private LuaBinding getBindings(final SecurityLevel securityLevel) {
		LuaTable t = new LuaTable();

		/* Use reflection to find and bind any methods annotated using @ScriptAPI
		 *  that have the appropriate security level */
		Stream.of(this.getClass().getDeclaredMethods())
				.filter(method -> {
					ScriptAPI annotation = method.getDeclaredAnnotation(ScriptAPI.class);
					return annotation != null && annotation.value().level <= securityLevel.level;
				})
				.forEach(method ->
						t.set(Optional.ofNullable(method.getDeclaredAnnotation(BindTo.class))
										.map(BindTo::value)
										.orElse(method.getName()),
								evalMethod(this, method)));

		/* Use reflection to find and bind any fields annotated using @BindField
		 *  that have the appropriate security level */
		Stream.of(this.getClass().getDeclaredFields())
				.filter(field -> {
					BindField annotation = field.getDeclaredAnnotation(BindField.class);
					return annotation != null && annotation.value().level <= securityLevel.level;
				})
				.forEach(field -> {
					try {
						field.setAccessible(true);
						t.set(Optional.ofNullable(field.getDeclaredAnnotation(BindTo.class))
										.map(BindTo::value)
										.orElse(field.getName()),
								CoerceJavaToLua.coerce(field.get(this)));
					}
					catch (Exception e) { }
				});
		return new LuaBinding(this.name, t);
	}

	@FunctionalInterface
	private interface LuaValueSupplier<T> {
		T get() throws InvocationTargetException, MethodNotOnWhitelistException, IllegalAccessException;
	}

	private LuaValue filterWhitelist(Method m, LuaValueSupplier<Object> fn)
			throws MethodNotOnWhitelistException, InvocationTargetException, IllegalAccessException {
		if(this.whitelist.onWhitelist(m.getName())) {
			return CoerceJavaToLua.coerce(fn.get());
		}
		else
			throw new MethodNotOnWhitelistException(m);
	}

	private LuaValue evalMethod(final Object caller, final Method m) {
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
						return filterWhitelist(m, () -> m.invoke(caller, args));
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
							return filterWhitelist(m, () -> m.invoke(caller));
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
							return filterWhitelist(m, () -> m.invoke(caller, arg));
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
							return filterWhitelist(m, () -> m.invoke(caller, arg1, arg2));
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
							return filterWhitelist(m, () -> m.invoke(caller, arg1, arg2, arg3));
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

	/**
	 * Gens a default Whitelist composed of all of the Methods available to the class.
	 * @return A Whitelist of allowed methods
	 */
	public Whitelist defaultWhitelist() {
		return new Whitelist()
				.addTo(Stream.of(this.getClass().getDeclaredMethods())
				.map(Method::getName)
				.distinct()
				.toArray());
	}
}
