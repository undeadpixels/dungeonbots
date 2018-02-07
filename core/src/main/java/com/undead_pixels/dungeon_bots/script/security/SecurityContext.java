package com.undead_pixels.dungeon_bots.script.security;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

public class SecurityContext {

	static Whitelist activeWhitelist = null;
	static SecurityLevel activeSecurityLevel = SecurityLevel.DEBUG;

	public synchronized static void set(LuaSandbox sandbox) {
		activeWhitelist = sandbox.getWhitelist();
		activeSecurityLevel = sandbox.getSecurityLevel();
	}

	public synchronized static void set(Whitelist w) {
		activeWhitelist = w;
	}

	public synchronized static void set(SecurityLevel _securityContext) {
		activeSecurityLevel = _securityContext;
	}

	public synchronized static Whitelist getWhitelist() {
		return activeWhitelist;
	}

	public synchronized static SecurityLevel getActiveSecurityLevel() {
		return activeSecurityLevel;
	}
}
