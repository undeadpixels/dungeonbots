package com.undead_pixels.dungeon_bots.script.interfaces;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import com.undead_pixels.dungeon_bots.script.proxy.LuaBinding;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;
import com.undead_pixels.dungeon_bots.script.proxy.LuaReflection;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.security.Whitelist;
import org.luaj.vm2.LuaValue;

import java.lang.reflect.*;
import java.util.Optional;
import java.util.stream.Stream;

import static com.undead_pixels.dungeon_bots.script.LuaSandbox.id;

/**
 * Interface for exposing or providing LuaBindings and Whitelist security features
 * for datatypes that wish to have a Lua facade
 */
public interface GetLuaFacade {

	int getId();
	String getName();

	/**
	 * Get a LuaValue of this object.
	 * @return A LuaTable decorated with LuaFunctions that will invoke the methods of <br>this object.
	 */
	default LuaValue getLuaValue() {
		return LuaProxyFactory.getLuaValue(this);
	}

	/**
	 * Get a LuaBinding object that is a tuple containing the name and LuaValue of the desired binding.
	 * @return A LuaBining for this object.
	 */
	default LuaBinding getLuaBinding() {
		return new LuaBinding(getName(), getLuaValue());
	}

	/**
	 * Generates a Whitelist of Methods for the target class not exceeding the given security level.
	 * @param clz The Class to create a template Whitelist of.
	 * @param securityLevel The desired SecurityLevel of the Whitelist.
	 * @return A Whitelist containing the id's of Methods of the target Class.
	 */
	static Whitelist getWhitelist(final Class<?> clz, final SecurityLevel securityLevel) {
		return LuaReflection.getWhitelist(LuaReflection.getBindableStaticMethods(clz), securityLevel);
	}

	/**
	 * Generates a Whitelist of Methods for this object not exceeding the given security level.
	 * @param securityLevel The desired SecurityLevel of the Whitelist.
	 * @return A Whitelist containing the id's of Methods of this object.
	 */
	default Whitelist getWhitelist(final SecurityLevel securityLevel) {
		return LuaReflection.getWhitelist(LuaReflection.getBindableMethods(this), this, securityLevel);
	}

	/**
	 * Generates a Whitelist of Methods for this object using the security level of the current Security context.
	 * @return A Whitelist containing the id's of Methods of this object.
	 */
	default Whitelist getWhitelist() {
		return this.getWhitelist(SecurityContext.getActiveSecurityLevel());
	}

	/**
	 * Generates a Whitelist of Methods of the target class using the security level of the current Security context.
	 * @param clz The Class to create a template Whitelist of.
	 * @return A Whitelist containing the id's of Methods of the target class.
	 */
	static Whitelist getWhitelist(final Class<?> clz) {
		return GetLuaFacade.getWhitelist(clz, SecurityContext.getActiveSecurityLevel());
	}

	/**
	 * Returns a Stream of Methods in the target Class that have been annotated with the {@code @Bind} symbol
	 * @param clz
	 * @return
	 */
	static Stream<Method> getBindableStaticMethods(final Class<?> clz) {
		return LuaReflection.getBindableStaticMethods(clz);
	}

	/**
	 * Returns a Stream of Methods of this Object that have been annotated with the {@code @Bind} symbol
	 * <pre>
	 *     {@code
	 *     // Get a Stream of all Methods annotated with @Bind
	 *     World w = new World();
	 *     Stream<Method> s = w.getBindableMethods();
	 *     }
	 * </pre>
	 * @return A Stream of Methods
	 */
	default Stream<Method> getBindableMethods() {
		return LuaReflection.getBindableMethods(this);
	}

	/**
	 * Returns a Stream of Fields of this Object that have been annotated with the {@code @Bind} symbol
	 * @return A Stream of Fields
	 */
	default Stream<Field> getBindableFields() {
		return LuaReflection.getBindableFields(this.getClass());
	}

	/**
	 * Returns a Stream of Fields of the target class that have been annotated with the {@code @Bind} symbol
	 * @param clz The target class
	 * @return A Stream of Fields
	 */
	static Stream<Field> getBindableStaticFields(final Class<?> clz) {
		return LuaReflection.getBindableStaticFields(clz);
	}

	/**
	 * Helper function that determines the name of the Element to use when Binding by
	 * using the existing value found in the @BindTo annotation if it exist, or defaulting
	 * to the Member name otherwise.
	 * @param m The Annotated Element to query for the @BindTo annoation
	 * @param <T> An element of type AnnotatedElement and Member
	 * @return The String name to bind the target element to.
	 */
	static <T extends AnnotatedElement & Member> String bindTo(final T m) {
		return Optional.ofNullable(m.getDeclaredAnnotation(BindTo.class))
				.map(BindTo::value)
				.orElse(m.getName());
	}
}
