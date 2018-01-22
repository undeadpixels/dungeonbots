package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.utils.annotations.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Pretty much everything visible/usable within a regular game. Does not include UI elements.
 */
public abstract class Entity implements BatchRenderable {
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

	private LuaBinding getBindings(SecurityLevel securityLevel) {
		LuaTable t = new LuaTable();
		for(Method method : this.getClass().getDeclaredMethods())
			Optional.ofNullable(method.getDeclaredAnnotation(ScriptAPI.class)).ifPresent(annotation -> {
			    if(annotation.value().level <= securityLevel.level)
			    	t.set(Optional.ofNullable(method.getDeclaredAnnotation(BindTo.class))
									.map(a -> a.value())
									.orElse(method.getName()),
							evalMethod(this, method, annotation));
			});
		for(Field f : this.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			Optional.ofNullable(f.getDeclaredAnnotation(BindField.class)).ifPresent(anno -> {
				if(anno.value().level <= securityLevel.level) {
					try {
						t.set(Optional.ofNullable(f.getDeclaredAnnotation(BindTo.class))
										.map(a -> a.value())
										.orElse(f.getName()),
								CoerceJavaToLua.coerce(f.get(this)));
					}catch (Exception e) { }
				}
			});
		}
		return new LuaBinding(this.name, t);
	}

	private LuaValue evalMethod(Object caller, Method m, ScriptAPI scriptAPI) {
		m.setAccessible(true);
		Class<?>[] paramTypes = m.getParameterTypes();
		Class<?> returnType = m.getReturnType();

		// If the expected return type of the function is Varargs or the only parameter is a Varargs treat the function
		// like its of type VarArgFunction
		if(returnType.equals(Varargs.class) || (paramTypes.length == 1 && paramTypes[0].equals(Varargs.class))) {
			class Vararg extends VarArgFunction {

				@Override
				public Varargs invoke(Varargs args) {
					try {
						assert Stream.of(paramTypes).allMatch(Varargs.class::equals);
						return CoerceJavaToLua.coerce(m.invoke(caller, args));
					}
					catch (Exception e) { return null; }
				}
			}
			return CoerceJavaToLua.coerce(new Vararg());
		}
		// Otherwise expect 1, 2 or 3 parameters for the method
		switch(paramTypes.length) {
			case 0:
				class ZeroArg extends ZeroArgFunction {
					@Override
					public LuaValue call() {
						try {
							return CoerceJavaToLua.coerce(m.invoke(caller));
						}
						catch (Exception e) { return null; }
					}
				}
				return CoerceJavaToLua.coerce(new ZeroArg());
			case 1:
				class OneArg extends OneArgFunction {
					@Override
					public LuaValue call(LuaValue arg) {
						try {
							assert Stream.of(paramTypes).allMatch(LuaValue.class::equals);
							return CoerceJavaToLua.coerce(m.invoke(caller, arg));
						}
						catch (Exception e) { return null; }
					}
				}
				return CoerceJavaToLua.coerce(new OneArg());
			case 2:
				class TwoArg extends TwoArgFunction {
					@Override
					public LuaValue call(LuaValue arg1, LuaValue arg2) {
						try {
							assert Stream.of(paramTypes).allMatch(LuaValue.class::equals);
							return CoerceJavaToLua.coerce(m.invoke(caller, arg1, arg2));
						}
						catch (Exception e) {
							e.printStackTrace();
							return null; }
					}
				}
				return CoerceJavaToLua.coerce(new TwoArg());
			case 3:
				class ThreeArg extends ThreeArgFunction {
					@Override
					public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
						try {
							assert Stream.of(paramTypes).allMatch(LuaValue.class::equals);
							return CoerceJavaToLua.coerce(m.invoke(caller, arg1, arg2, arg3));
						}
						catch (Exception e) { return null; }
					}
				}
				return CoerceJavaToLua.coerce(new ThreeArg());
			default:
		}
		return LuaValue.NIL;
	}

	/**
	 * Generates a LuaScriptEnvironment for the given entity
	 * @param securityLevel The Security level of the requested LuaScriptEnvironment
	 * @return
	 */
	public LuaScriptEnvironment getScriptEnvironment(SecurityLevel securityLevel) {
	    LuaScriptEnvironment scriptEnvironment = new LuaScriptEnvironment(securityLevel);
	    scriptEnvironment.add(getBindings(securityLevel));
	    return scriptEnvironment;
    }

	/**
	 * Generates a LuaScriptEnvironment for the given entity
	 * @return
	 */
	public LuaScriptEnvironment getScriptEnvironment() {
		return getScriptEnvironment(SecurityLevel.AUTHOR);
	}
}
