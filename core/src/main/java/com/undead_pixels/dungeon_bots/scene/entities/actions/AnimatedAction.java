package com.undead_pixels.dungeon_bots.scene.entities.actions;

public abstract class AnimatedAction implements Action {
	private float currentTime = 0;
	private float duration;

	public AnimatedAction(float duration) {
		super();
		this.duration = duration;
	}

	@Override
	public final boolean act(float dt) {
		currentTime += dt;
		
		// TODO - nonlinear splines of time
		
		float timeFraction = currentTime / duration;
		
		if(timeFraction > 1) {
			timeFraction = 1;
		}
		animateAtTimeFraction(timeFraction);
		System.out.println("tf = "+timeFraction);
		

		return timeFraction >= 1;
	}
	
	public abstract void animateAtTimeFraction(float timeFraction);
	
}