package com.undead_pixels.dungeon_bots.nogdx;

public abstract class Animation {

	/**How many seconds have elapsed since the animation started.*/
	protected float time;
	private final long beginTime = System.nanoTime();
	/**The life cycle of the animation.*/
	protected final float timeToLive;


	protected Animation(float timeToLive) {
		this.timeToLive = timeToLive;
	}


	/**NOTE:  animations use their own timers because it turns out that dt from 
	 * the rendering loop is not very steady.*/
	public final boolean draw(RenderingContext batch, TextureRegion tex, float x, float y, float rotation, float scaleX,
			float scaleY) {
		
		if (!isActive()) return false;
		long currentTime = System.nanoTime();
		float newTime = (currentTime - beginTime) / 1_000_000_000.0f;

		float dt;
		if (newTime > timeToLive) {
			dt = timeToLive - time;
			this.time = timeToLive;
		} else {
			dt = newTime - time;
			this.time = newTime;
		}
		drawFrame(batch, tex, x, y, rotation, scaleX, scaleY, dt);
		return true;
	}


	/**Override to actually draw an animation frame.*/
	public abstract void drawFrame(RenderingContext batch, TextureRegion tex, float x, float y, float rotation,
			float scaleX, float scaleY, float dt);


	/**Returns whether an animation is active, meaning it has not come to the end of its life cycle yet.*/
	public final boolean isActive() {
		return time < timeToLive;
	}


	/**The parameterized life time from 0 to 1.*/
	public float getLifetime() {
		return time / timeToLive;
	}


}
