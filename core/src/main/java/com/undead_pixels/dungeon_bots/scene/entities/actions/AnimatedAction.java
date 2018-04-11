package com.undead_pixels.dungeon_bots.scene.entities.actions;

/**
 * An action that prefers its time in a value increasing from 0 to 1 instead of just delta in time
 */
public abstract class AnimatedAction implements Action {
	/**
	 * A counter to keep track of the current time, in seconds
	 */
	private float currentTime = 0;
	
	/**
	 * The duration of this animation, in seconds
	 */
	private float duration;

	/**
	 * @param duration	The duration of this animation, in seconds
	 */
	public AnimatedAction(float duration) {
		super();
		this.duration = duration;
	}

	/**
	 * Do not override.
	 * 
	 * Calls the animateAtTimeFraction(float) function with the correct time
	 */
	@Override
	public final boolean act(float dt) {
		currentTime += dt;
		
		float timeFraction = currentTime / duration;
		
		if(timeFraction > 1) {
			timeFraction = 1;
		}
		
		animateAtTimeFraction(timeFraction);

		return timeFraction >= 1;
	}
	
	/**
	 * A function to update the current animation.
	 * 
	 * @param timeFraction	A number ranging from 0 (just started) to 1 (finishing) indicating progress.
	 */
	public abstract void animateAtTimeFraction(float timeFraction);
	
}