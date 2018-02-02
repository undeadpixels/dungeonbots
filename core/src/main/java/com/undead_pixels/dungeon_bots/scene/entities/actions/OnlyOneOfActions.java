package com.undead_pixels.dungeon_bots.scene.entities.actions;

/**
 * Given a list of actions, only executes the first one that doesn't fail.
 * 
 * Helpful for saying "If this action fails, play some kind of failure animation"
 */
public class OnlyOneOfActions implements Action {

	
	/**
	 * A list of actions, in order of priority
	 */
	private Action[] innerActions;
	
	public OnlyOneOfActions(Action... actions) {
		innerActions = actions;
	}
	
	/**
	 * Index of the child action
	 */
	private int currentIdx;

	@Override
	public boolean preAct() {
		// check if the inner actions are ready to act
		while(! innerActions[currentIdx].preAct()) {

			currentIdx++;
			if(currentIdx >= innerActions.length) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean act(float dt) {
		// call the current child action
		return innerActions[currentIdx].act(dt);
	}
	
	@Override
	public void postAct() {
		// call the current child's postAct
		innerActions[currentIdx].postAct();
	}
}
