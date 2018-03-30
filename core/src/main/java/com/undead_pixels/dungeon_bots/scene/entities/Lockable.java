package com.undead_pixels.dungeon_bots.scene.entities;

public interface Lockable {
	boolean isLocked();
	void lock();
	void unlock();
}
