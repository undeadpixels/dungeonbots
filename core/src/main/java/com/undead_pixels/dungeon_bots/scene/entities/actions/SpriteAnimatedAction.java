package com.undead_pixels.dungeon_bots.scene.entities.actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class SpriteAnimatedAction extends AnimatedAction {
	public SpriteAnimatedAction(Sprite sprite, float duration) {
		super(duration);
		this.sprite = sprite;
	}
	private Sprite sprite;
	
	private boolean didHaveFirstFrame = false;
	private float x0 = Float.NaN, y0 = Float.NaN, sx0 = Float.NaN, sy0 = Float.NaN, r0 = Float.NaN;
	private float x1 = Float.NaN, y1 = Float.NaN, sx1 = Float.NaN, sy1 = Float.NaN, r1 = Float.NaN;
	
	public void setFinalPosition(float x, float y) {
		x1 = x;
		y1 = y;
	}
	public void setFinalScale(float sx, float sy) {
		sx1 = sx;
		sy1 = sy;
	}
	public void setFinalScale(float scale) {
		sx1 = scale;
		sy1 = scale;
	}
	public void setFinalRotation(float rotation) {
		r1 = rotation;
	}
	
	/**
	 * Linear interpolate
	 * 
	 * @param a
	 * @param b
	 * @param t
	 * @return
	 */
	private static final float lerp(float a, float b, float t) {
		return a + (b-a)*t;
	}
	
	public final void animateAtTimeFraction(float timeFraction) {
		if(!didHaveFirstFrame) {
			x0 =  sprite.getX();
			y0 =  sprite.getY();
			sx0 = sprite.getScaleX();
			sy0 = sprite.getScaleY();
			r0 =  sprite.getRotation();

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
		
		float t = timeFraction;
		sprite.setPosition(lerp(x0, x1, t), lerp(y0, y1, t));
		sprite.setScale(lerp(sx0, sx1, t), lerp(sy0, sy1, t));
		sprite.setRotation(lerp(r0, r1, t));
	}
}