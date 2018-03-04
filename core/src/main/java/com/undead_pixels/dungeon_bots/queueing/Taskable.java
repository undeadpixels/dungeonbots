package com.undead_pixels.dungeon_bots.queueing;

/**
 * Some arbitrary thing that can be put into a task queue
 */
public interface Taskable<T> {
	
	/**
	 * Do any initial setup that must happen before this action is started
	 * 
	 * @return	true if the action will occur as planned; false if it will fail
	 */
	public default boolean preAct() { return true; }
	
	/**
	 * Continue performing/animating this action every frame.
	 * 
	 * @return	true once the action is completed, at which point postAct() will be called.
	 */
	public boolean act(float dt);
	
	/**
	 * Do any final teardown or state changes that need to happen after this action has completed
	 */
	public default void postAct() {}
	
}
