package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;

public final class AnimationVibrate extends Animation {


	private final float shakes;
	private final float amplitude;


	public AnimationVibrate() {
		this(20f, 0.25f, 3.0f);
	}


	/**@param shakes Shakes per second.*/
	public AnimationVibrate(float shakes, float amplitude, float timeToLive) {
		super(timeToLive);
		this.shakes = shakes;
		this.amplitude = amplitude;
	}


	@Override
	public void drawFrame(RenderingContext batch, TextureRegion tex, float x, float y, float rotation, float scaleX,
			float scaleY, float dt) {
		if (tex == null)
			return;
		
		float lifeTime = this. getLifetime();
		float deflect = (float) Math.sin(lifeTime * shakes * Math.PI * 2) * ((1-lifeTime)*amplitude);
		AffineTransform xform;
		if (rotation == 0) {
			xform = AffineTransform.getTranslateInstance((.5f - .5f * scaleX) + x + deflect, .5f + .5f * scaleY + y);
			xform.scale(scaleX / tex.getW(), -scaleY / tex.getH());
		} else {
			xform = AffineTransform.getTranslateInstance(.5 + x, .5 * scaleY + y);
			xform.rotate(rotation);
			xform.scale(scaleX / tex.getW(), -scaleY / tex.getH());
			xform.translate(-.5, .5);
		}
		batch.draw(tex, xform);


	}


}
