package com.undead_pixels.dungeon_bots.scene.entities;

public interface Pushable {
	public void push(final Actor.Direction direction);
	public void bumpedInto(final Entity e, final Actor.Direction direction);
}
