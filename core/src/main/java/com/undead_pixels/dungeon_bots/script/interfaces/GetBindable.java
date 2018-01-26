package com.undead_pixels.dungeon_bots.script.interfaces;
import com.undead_pixels.dungeon_bots.script.LuaReflection;
import com.undead_pixels.dungeon_bots.script.Whitelist;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.undead_pixels.dungeon_bots.script.LuaSandbox.id;
import static com.undead_pixels.dungeon_bots.script.LuaSandbox.staticId;

public interface GetBindable {

	static Whitelist permissiveWhitelist(final Class<?> clz) {
		return LuaReflection.getPermissiveWhitelist(getBindableStaticMethods(clz).collect(Collectors.toList()));
	}

	default Whitelist permissiveWhitelist() {
		return LuaReflection.getPermissiveWhitelist(this.getBindableMethods().collect(Collectors.toList()), this);
	}

	static Stream<Method> getBindableStaticMethods(final Class<?> clz) {
		return LuaReflection.getBindableStaticMethods(clz, SecurityLevel.DEBUG);
	}


	default Stream<Method> getBindableMethods() {
		return LuaReflection.getBindableMethods(this, SecurityLevel.DEBUG);
	}

	default Stream<Method> getBindableMethods(final SecurityLevel securityLevel) {
		return LuaReflection.getBindableMethods(this, securityLevel);
	}

	static Stream<Method> getBindableStaticMethods(final Class<?> clz, final SecurityLevel securityLevel) {
		return LuaReflection.getBindableStaticMethods(clz, securityLevel);
	}

	default Stream<Field> getBindableFields() {
		return LuaReflection.getBindableFields(this.getClass(), SecurityLevel.DEBUG);
	}

	default Stream<Field> getBindableFields(final SecurityLevel securityLevel) {
		return LuaReflection.getBindableFields(this.getClass(), securityLevel);
	}

	static Stream<Field> getBindableStaticFields(final Class<?> clz) {
		return LuaReflection.getBindableStaticFields(clz, SecurityLevel.DEBUG);
	}

	static Stream<Field> getBindableStaticFields(final Class<?> clz, final SecurityLevel securityLevel) {
		return LuaReflection.getBindableStaticFields(clz, securityLevel);
	}

	static <T extends AnnotatedElement & Member> String bindTo(final T m) {
		return Optional.ofNullable(m.getDeclaredAnnotation(BindTo.class))
				.map(BindTo::value)
				.orElse(m.getName());
	}
}
