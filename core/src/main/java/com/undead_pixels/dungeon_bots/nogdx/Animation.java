package com.undead_pixels.dungeon_bots.nogdx;

public abstract class Animation {

	protected float time;
	protected final float timeToLive;


	protected Animation(float timeToLive) {
		this.timeToLive = timeToLive;
	}


	public abstract void draw(RenderingContext batch, TextureRegion tex, float x, float y, float rotation, float scaleX,
			float scaleY, float dt);


	public final boolean isActive() {
		return time < timeToLive;
	}


	public float getTimeRemaining() {
		return timeToLive - time;
	}
}
