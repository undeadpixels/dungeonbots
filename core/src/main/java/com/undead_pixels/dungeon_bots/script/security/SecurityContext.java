package com.undead_pixels.dungeon_bots.script.security;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SecurityContext {

	private static ReadWriteLock whitelistLock = new ReentrantReadWriteLock();
	private static ReadWriteLock securitylevelLock = new ReentrantReadWriteLock();

	static Map<Long, Whitelist> whitelistMap = new HashMap<>();
	static Map<Long, SecurityLevel> securityLevelMap = new HashMap<>();

	public static void set(Long l, LuaSandbox sandbox) {
		whitelistLock.writeLock().lock();
		try {
			whitelistMap.put(l, sandbox.getWhitelist());
		}
		finally {
			whitelistLock.writeLock().unlock();
		}

		securitylevelLock.writeLock().lock();
		try {
			securityLevelMap.put(l, sandbox.getSecurityLevel());
		}
		finally {
			securitylevelLock.writeLock().unlock();
		}
	}

	public static void set(LuaSandbox sandbox) {
		set(Thread.currentThread().getId(), sandbox);
	}

	public static void set(Long l, Whitelist w) {
		whitelistLock.writeLock().lock();
		try {
			whitelistMap.put(l, w);
		}
		finally {
			whitelistLock.writeLock().unlock();
		}
	}

	public static void set(Whitelist w) {
		set(Thread.currentThread().getId(), w);
	}

	public static void set(Long l, SecurityLevel securityLevel) {
		securitylevelLock.writeLock().lock();
		try {
			securityLevelMap.put(l, securityLevel);
		}
		finally {
			securitylevelLock.writeLock().unlock();
		}
	}

	public static void set(SecurityLevel securityLevel) {
		set(Thread.currentThread().getId(), securityLevel);
	}

	public static Whitelist getWhitelist() {
		whitelistLock.readLock().lock();
		Whitelist ans;
		try {
			ans = whitelistMap.get(Thread.currentThread().getId());
		}
		finally {
			whitelistLock.readLock().unlock();
		}
		return ans == null ? new Whitelist() : ans;
	}

	public static SecurityLevel getActiveSecurityLevel() {
		SecurityLevel securityLevel = securityLevelMap.get(Thread.currentThread().getId());
		return securityLevel == null ? SecurityLevel.DEBUG : securityLevel;
	}

	public static void remove(Long id) {
		whitelistLock.writeLock().lock();
		try {
			whitelistMap.remove(id);
		}
		finally {
			whitelistLock.writeLock().unlock();
		}
		securitylevelLock.writeLock().lock();
		try {
			securityLevelMap.remove(id);
		}
		finally {
			securitylevelLock.writeLock().unlock();
		}
	}


	public static void remove() {
		remove(Thread.currentThread().getId());
	}

}
