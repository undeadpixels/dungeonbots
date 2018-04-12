package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;

public class AnimationJump extends Animation {

	private final static float G = -9.8f;
	private final float v_0;
	/**Relates a sprite size to the world size.*/
	private static final float scalar = 0.25f;


	public AnimationJump() {
		this(1f);
	}


	public AnimationJump(float howHigh) {
		super((float) Math.sqrt(howHigh / -G) * 2);
		// this.howHigh = howHigh;
		this.v_0 = (timeToLive / 2) * -G;

	}


	@Override
	public void drawFrame(RenderingContext batch, TextureRegion tex, float x, float y, float rotation, float scaleX,
			float scaleY, float dt) {
		if (tex == null)
			return;


		float elevation = (G * time * time / 2) + (v_0 * time) + 0;
		// System.out.println("G:" + G + "\t v_0:" + v_0 + "\t elevation: " +
		// elevation);

		AffineTransform xform;
		if (rotation == 0) {
			xform = AffineTransform.getTranslateInstance((.5f - .5f * scaleX) + x,
					.5f + .5f * scaleY + y + (elevation * scaleY * tex.getH() * scalar));
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
