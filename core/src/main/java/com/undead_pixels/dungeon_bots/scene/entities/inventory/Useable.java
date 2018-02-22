package com.undead_pixels.dungeon_bots.scene.entities;

public interface Useable {
	default boolean use() {
		return false;
	}
}
