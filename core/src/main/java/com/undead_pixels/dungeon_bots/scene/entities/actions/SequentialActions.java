package com.undead_pixels.dungeon_bots.scene.entities.actions;

/**
 * A list of actions
 */
public final class SequentialActions implements Action {
	
	/**
	 * A list of actions, to be performed sequentially
	 */
	private Action[] innerActions;
	
	public SequentialActions(Action... actions) {
		innerActions = actions;
	}
	
	/**
	 * Index of the child action currently being executed
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
		boolean currentFinished = innerActions[currentIdx].act(dt);
		
		if(currentFinished) {
			// action finished, so let it postAct
			innerActions[currentIdx].postAct();
			currentIdx++;
			
			if(currentIdx >= innerActions.length) {
				return true;
			}
			
			if(!preAct()) {
				// if we have no more valid preAct's, then indicate that we're finished.
				return true;
			}
		}
		
		return false;
	}
}