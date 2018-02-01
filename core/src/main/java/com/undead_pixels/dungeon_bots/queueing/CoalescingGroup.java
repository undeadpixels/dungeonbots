package com.undead_pixels.dungeon_bots.queueing;

/**
 * A function capable of coalescing two objects into one.
 * 
 * For example, two lua update() events could be coalesced into one,
 * causing only one call to happen (perhaps adding the two dt's together)
 *
 * @param <T>	A type of objects to coalesce
 */
public abstract class CoalescingGroup<T> {

	/**
	 * Coalesces two objects together: the first one into the second one.
	 * After that, the second object should contain the same general idea
	 * as both the first and the second.
	 * 
	 * @param otherT		The new object to coalesce from
	 * @param t			The object to coalesce into
	 */
	public abstract void coalesce(T otherT, T t);

}
