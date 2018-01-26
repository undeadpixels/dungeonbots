package com.undead_pixels.dungeon_bots.scene.entities.actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class SpriteAnimatedAction extends AnimatedAction {
	public SpriteAnimatedAction(Sprite sprite, float duration) {
		super(duration);
		this.sprite = sprite;
	}
	private Sprite sprite;
	
	private boolean didHaveFirstFrame = false;
	private float duration;
	private float x0, y0, sx0, sy0, r0;
	private float x1, y1, sx1, sy1, r1;
	
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
			didHaveFirstFrame = true;
		}
		
		float t = timeFraction;
		sprite.setPosition(lerp(x0, x1, t), lerp(y0, y1, t));
		sprite.setScale(lerp(sx0, sx1, t), lerp(sy0, sy1, t));
		sprite.setRotation(lerp(r0, r1, t));
	}
}