package com.undead_pixels.dungeon_bots.scene.entities.actions;

public interface Action {
	
	/**
	 * Do any initial setup that must happen before this action is started
	 * 
	 * @return	true if the action will occur as planned; false if it will fail
	 */
	default public boolean preAct() { return true; }
	
	/**
	 * Continue performing/animating this action every frame.
	 * 
	 * @return	true once the action is completed, at which point postAct() will be called.
	 */
	public boolean act(float dt);
	
	/**
	 * Do any final teardown or state changes that need to happen after this action has completed
	 */
	default public void postAct() {}
}
