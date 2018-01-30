package com.undead_pixels.dungeon_bots.script.interfaces;
import com.undead_pixels.dungeon_bots.script.*;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import org.luaj.vm2.LuaValue;

import java.lang.reflect.*;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.undead_pixels.dungeon_bots.script.LuaSandbox.id;

public interface GetBindable {

	int getId();
	String getName();

	default LuaValue getLuaValue() {
		return LuaProxyFactory.getLuaValue(this);
	}

	default LuaBinding getLuaBinding() {
		return new LuaBinding(getName(), getLuaValue());
	}

	static Whitelist getWhitelist(final Class<?> clz, final SecurityLevel securityLevel) {
		return LuaReflection.getWhitelist(getBindableStaticMethods(clz), securityLevel);
	}

	default Whitelist getWhitelist(final SecurityLevel securityLevel) {
		return LuaReflection.getWhitelist(this.getBindableMethods(), this, securityLevel);
	}

	default Whitelist getWhitelist() {
		return this.getWhitelist(SecurityContext.getActiveSecurityLevel());
	}

	static Whitelist getWhitelist(final Class<?> clz) {
		return GetBindable.getWhitelist(clz, SecurityContext.getActiveSecurityLevel());
	}

	static Stream<Method> getBindableStaticMethods(final Class<?> clz) {
		return LuaReflection.getBindableStaticMethods(clz);
	}


	default Stream<Method> getBindableMethods() {
		return LuaReflection.getBindableMethods(this);
	}

	default Stream<Method> getBindableMethods(final SecurityLevel securityLevel) {
		return LuaReflection.getBindableMethods(this);
	}

	default Stream<Field> getBindableFields() {
		return LuaReflection.getBindableFields(this.getClass());
	}

	static Stream<Field> getBindableStaticFields(final Class<?> clz) {
		return LuaReflection.getBindableStaticFields(clz);
	}

	static <T extends AnnotatedElement & Member> String bindTo(final T m) {
		return Optional.ofNullable(m.getDeclaredAnnotation(BindTo.class))
				.map(BindTo::value)
				.orElse(m.getName());
	}
}
