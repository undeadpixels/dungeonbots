package com.undead_pixels.dungeon_bots.script;

import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

public class SecurityContext {

	static Whitelist activeWhitelist = null;
	static SecurityLevel activeSecurityLevel = SecurityLevel.DEBUG;

	public synchronized static void set(LuaSandbox sandbox) {
		activeWhitelist = sandbox.getWhitelist();
		activeSecurityLevel = sandbox.getSecurityLevel();
	}

	public static void set(Whitelist w) {
		activeWhitelist = w;
	}

	public static void set(SecurityLevel _securityContext) {
		activeSecurityLevel = _securityContext;
	}

	public static Whitelist getWhitelist() {
		return activeWhitelist;
	}

	public static SecurityLevel getActiveSecurityLevel() {
		return activeSecurityLevel;
	}
}
