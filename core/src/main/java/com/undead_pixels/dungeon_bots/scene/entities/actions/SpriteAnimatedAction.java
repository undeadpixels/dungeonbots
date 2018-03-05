package com.undead_pixels.dungeon_bots.scene.entities.actions;

import com.undead_pixels.dungeon_bots.nogdx.Sprite;

/**
 * An action that involves animating a sprite
 */
public abstract class SpriteAnimatedAction extends AnimatedAction {
	
	/**
	 * Constructor
	 * 
	 * @param sprite		The sprite to animate
	 * @param duration	How long this action/animation takes, in seconds
	 */
	public SpriteAnimatedAction(Sprite sprite, float duration) {
		super(duration);
		this.sprite = sprite;
	}
	
	/**
	 * The sprite to animate
	 */
	private Sprite sprite;
	
	/**
	 * A boolean indicating if this animation has started yet
	 */
	private boolean didHaveFirstFrame = false;
	
	/**
	 * Initial transform info
	 */
	private float x0 = Float.NaN, y0 = Float.NaN, sx0 = Float.NaN, sy0 = Float.NaN, r0 = Float.NaN;
	
	/**
	 * Final transform info
	 */
	private float x1 = Float.NaN, y1 = Float.NaN, sx1 = Float.NaN, sy1 = Float.NaN, r1 = Float.NaN;
	
	/**
	 * Set the final position to animate to
	 * 
	 * @param x
	 * @param y
	 */
	public void setFinalPosition(float x, float y) {
		x1 = x;
		y1 = y;
	}
	
	/**
	 * Set the final scale to animate to
	 * 
	 * @param sx		Scale in x dimension
	 * @param sy		Scale in y dimension
	 */
	public void setFinalScale(float sx, float sy) {
		sx1 = sx;
		sy1 = sy;
	}
	
	/**
	 * Set the final scale to animate to
	 * 
	 * @param scale
	 */
	public void setFinalScale(float scale) {
		sx1 = scale;
		sy1 = scale;
	}
	
	/**
	 * Set the final rotation to animate to
	 * 
	 * @param rotation
	 */
	public void setFinalRotation(float rotation) {
		r1 = rotation;
	}
	
	/**
	 * Linear interpolate
	 * 
	 * @param a	initial value
	 * @param b	final value
	 * @param t	interpolation factor
	 * @return
	 */
	private static final float lerp(float a, float b, float t) {
		return a + (b-a)*t;
	}
	
	/**
	 * Do not override.
	 * 
	 * Feel free to override preAct() and postAct() as they are not used by this animation.
	 */
	public final void animateAtTimeFraction(float timeFraction) {
		if(!didHaveFirstFrame) {
			// store initial position
			x0 =  sprite.getX();
			y0 =  sprite.getY();
			sx0 = sprite.getScaleX();
			sy0 = sprite.getScaleY();
			r0 =  sprite.getRotation();

			// if final positions haven't been set, set them to stay where they were
			if(Float.isNaN(x1)) {
				x1 = x0;
			}
			if(Float.isNaN(y1)) {
				y1 = y0;
			}
			if(Float.isNaN(sx1)) {
				sx1 = sx0;
			}
			if(Float.isNaN(sy1)) {
				sy1 = sy0;
			}
			if(Float.isNaN(r1)) {
				r1 = r0;
			}
			
			
			didHaveFirstFrame = true;
		}
		
		// perform the animation
		float t = timeFraction;
		sprite.setPosition(lerp(x0, x1, t), lerp(y0, y1, t));
		sprite.setScale(lerp(sx0, sx1, t), lerp(sy0, sy1, t));
		sprite.setRotation(lerp(r0, r1, t));
	}
}