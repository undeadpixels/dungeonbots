package com.undead_pixels.dungeon_bots.queueing;

public abstract class CoalescingGroup<T> {

	public abstract void coalesce(T otherT, T t);

}
