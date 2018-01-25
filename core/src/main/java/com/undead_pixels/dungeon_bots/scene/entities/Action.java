package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;

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
	
	
	public static abstract class AnimatedAction implements Action {
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
			return animateAtTime(currentTime / duration);
		}
		
		public abstract boolean animateAtTime(float currentTime);
		
	}
	
	public static final class SequentialActions implements Action {
		
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
	
	public static abstract class SpriteAnimatedAction extends AnimatedAction {
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
		
		public final boolean animateAtTime(float currentTime) {
			if(!didHaveFirstFrame) {
				x0 =  sprite.getX();
				y0 =  sprite.getY();
				sx0 = sprite.getScaleX();
				sy0 = sprite.getScaleY();
				r0 =  sprite.getRotation();
				didHaveFirstFrame = true;
			}
			
			float t = currentTime;
			sprite.setPosition(lerp(x0, x1, t), lerp(y0, y1, t));
			sprite.setScale(lerp(sx0, sx1, t), lerp(sy0, sy1, t));
			sprite.setRotation(lerp(r0, r1, t));
			
			
			
			return currentTime >= duration;
		}
	}
}
