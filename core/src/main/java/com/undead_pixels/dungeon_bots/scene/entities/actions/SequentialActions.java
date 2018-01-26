package com.undead_pixels.dungeon_bots.scene.entities.actions;

public final class SequentialActions implements Action {
	
	private Action[] innerActions;
	private int currentIdx;

	@Override
	public boolean preAct() {
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
		boolean currentFinished = innerActions[currentIdx].act(dt);
		
		if(currentFinished) {
			innerActions[currentIdx].postAct();
			currentIdx++;
			
			if(currentIdx >= innerActions.length) {
				return true;
			}
			
			if(!preAct()) {
				return false;
			}
		}
		
		return false;
	}
}