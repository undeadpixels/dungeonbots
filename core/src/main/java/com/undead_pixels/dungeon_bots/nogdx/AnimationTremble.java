package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;

public class AnimationTremble extends Animation {


	private final float shakesPerSecond;
	private final float amplitude;


	public AnimationTremble() {
		this(1f, 0.25f, 10.0f);
	}


	public AnimationTremble(float shakesPerSecond, float amplitude, float timeToLive) {
		super(timeToLive);
		this.shakesPerSecond = shakesPerSecond;
		this.amplitude = amplitude;
	}


	@Override
	public void draw(RenderingContext batch, TextureRegion tex, float x, float y, float rotation, float scaleX,
			float scaleY, float dt) {
		// The dt coming in doesn't appear to be in seconds. Choosing an
		// arbitrary factor of about 30 gets it to about seconds again.
		dt /= 30;
		time += dt;		
		if (tex != null) {
			float deflect = (float) Math.sin(time * shakesPerSecond) * ((getTimeRemaining() / timeToLive) * amplitude);
			AffineTransform xform;
			if (rotation == 0) {
				xform = AffineTransform.getTranslateInstance((.5f - .5f * scaleX) + x + deflect,
						.5f + .5f * scaleY + y);
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


}
