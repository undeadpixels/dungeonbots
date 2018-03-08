package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import java.io.Serializable;

public final class WeaponStats implements Serializable {
	final int damage;
	final int speed;
	final int range;

	WeaponStats(int damage, int speed, int range) {
		this.damage = damage;
		this.speed = speed;
		this.range = range;
	}
}
