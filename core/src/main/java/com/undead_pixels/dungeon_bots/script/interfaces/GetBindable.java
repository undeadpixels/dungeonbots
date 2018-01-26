package com.undead_pixels.dungeon_bots.script.interfaces;
import com.undead_pixels.dungeon_bots.script.LuaReflection;
import com.undead_pixels.dungeon_bots.script.Whitelist;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.undead_pixels.dungeon_bots.script.LuaSandbox.id;
import static com.undead_pixels.dungeon_bots.script.LuaSandbox.staticId;

public interface GetBindable {

	static Whitelist permissiveWhitelist(Class<?> clz) {
		return LuaReflection.getPermissiveWhitelist(getBindableStaticMethods(clz));
	}

	default Whitelist permissiveWhitelist() {
		return LuaReflection.getPermissiveWhitelist(this.getBindableMethods(), this);
	}

	static Collection<Method> getBindableStaticMethods(Class<?> clz) {
		return LuaReflection.getBindableStaticMethods(clz, SecurityLevel.DEBUG);
	}


	default Collection<Method> getBindableMethods() {
		return LuaReflection.getBindableMethods(this, SecurityLevel.DEBUG);
	}

	default Collection<Method> getBindableMethods(SecurityLevel securityLevel) {
		return LuaReflection.getBindableMethods(this, securityLevel);
	}

	static Collection<Method> getBindableStaticMethods(Class<?> clz, SecurityLevel securityLevel) {
		return LuaReflection.getBindableStaticMethods(clz, securityLevel);
	}

	default Collection<Field> getBindableFields() {
		return LuaReflection.getBindableFields(this.getClass(), SecurityLevel.DEBUG);
	}

	default Collection<Field> getBindableFields(SecurityLevel securityLevel) {
		return LuaReflection.getBindableFields(this.getClass(), securityLevel);
	}

	static Collection<Field> getBindableStaticFields(Class<?> clz) {
		return LuaReflection.getBindableStaticFields(clz, SecurityLevel.DEBUG);
	}

	static Collection<Field> getBindableStaticFields(Class<?> clz, SecurityLevel securityLevel) {
		return LuaReflection.getBindableStaticFields(clz, securityLevel);
	}

	static String bindTo(Method m) {
		return Optional.ofNullable(m.getDeclaredAnnotation(BindTo.class))
				.map(BindTo::value)
				.orElse(m.getName());
	}

	static String bindTo(Field m) {
		return Optional.ofNullable(m.getDeclaredAnnotation(BindTo.class))
				.map(BindTo::value)
				.orElse(m.getName());
	}

}
