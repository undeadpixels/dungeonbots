package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.*;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.utils.annotations.ScriptAPI;
import com.undead_pixels.dungeon_bots.utils.annotations.SecurityLevel;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import java.lang.reflect.Method;
import java.util.Optional;

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
	 * @param world		The world to contain this Actor
	 */
	public Entity(World world) {
		this(world, null);
		world.addEntity(this);
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user script that is run on this object
	 */
	public Entity(World world, LuaScript script) {
		super();
		this.world = world;
		this.script = script;
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
	 * @return		This Entity's position in screen space
	 */
	public abstract Vector2 getPosition();
	
	
	/**
	 * @return		If this object disallows movement through it
	 */
	public abstract boolean isSolid();

	public LuaBinding getBindings(SecurityLevel securityLevel) {
		LuaTable t = new LuaTable();
		for(Method method : this.getClass().getDeclaredMethods()) {
			Optional.ofNullable(method.getDeclaredAnnotation(ScriptAPI.class)).ifPresent(anno -> {
			    if(anno.value().level <= securityLevel.level) {
                    try {
                        // Attempt to call zero argument method
                        Object o = method.invoke(this);
                        if(LuaBinding.class.equals(o.getClass())) {
                            LuaBinding lb = LuaBinding.class.cast(o);
                            t.set(lb.bindTo, lb.luaValue);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
			});
		}
		return new LuaBinding("entity", t);
	}

	protected LuaBinding genZeroArg(final String bindTo, final Runnable r) {
		class ZeroArg extends ZeroArgFunction {
			@Override
			public LuaValue call() {
				r.run();
				return null;
			}
		}
		return new LuaBinding(bindTo, CoerceJavaToLua.coerce(r));
	}
}
