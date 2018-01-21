package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.utils.annotations.ScriptAPI;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
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
		for(Method method : this.getClass().getDeclaredMethods()) {
			Optional.ofNullable(method.getDeclaredAnnotation(ScriptAPI.class)).ifPresent(annotation -> {
			    if(annotation.value().level <= securityLevel.level)
			    	t.set(method.getName(), evalMethod(this, method, annotation));
			});
		}
		return new LuaBinding(this.name, t);
	}

	private LuaValue evalMethod(Object caller, Method m, ScriptAPI scriptAPI) {
		Class<?>[] paramTypes = m.getParameterTypes();
		Class<?> returnType = m.getReturnType();
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
				class SingleArg extends OneArgFunction {
					@Override
					public LuaValue call(LuaValue arg) {
						try {
							assert Stream.of(paramTypes).allMatch(LuaValue.class::equals);
							return CoerceJavaToLua.coerce(m.invoke(caller, arg));
						}
						catch (Exception e) { return null; }
					}
				}
				return CoerceJavaToLua.coerce(new SingleArg());
			case 2:
				break;
			case 3:
				break;
			default:
		}
		return LuaValue.NIL;
	}

	public LuaScriptEnvironment getScriptEnvironment(SecurityLevel securityLevel) {
	    LuaScriptEnvironment scriptEnvironment = new LuaScriptEnvironment();
	    scriptEnvironment.add(getBindings(securityLevel));
	    return scriptEnvironment;
    }

	protected LuaBinding genZeroArg(final String bindTo, final Runnable r) {
		class ZeroArg extends ZeroArgFunction {
			@Override
			public LuaValue call() {
				r.run();
				return null;
			}
		}
		return new LuaBinding(bindTo, CoerceJavaToLua.coerce(new ZeroArg()));
	}
}
