package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;

import com.undead_pixels.dungeon_bots.ui.UIBuilder;

public class AnimationHaunt extends Animation {

	
	private final float amplitude;


	public AnimationHaunt() {
		this(1f);
	}


	public AnimationHaunt(float amplitude) {
		super((float) (Math.PI * 2));
		this.amplitude = 1f;
		//UIBuilder.playSound("sounds/jalastram_jump.wav");
	}


	@Override
	public void drawFrame(RenderingContext batch, TextureRegion tex, float x, float y, float rotation, float scaleX,
			float scaleY, float dt) {

		double gamma = amplitude * Math.cos(3 * time + (Math.PI/3));
		float off_x = (float) (gamma * Math.sin(time));
		float off_y = (float) (gamma * Math.cos(time));


		AffineTransform xform;
		if (rotation == 0) {
			xform = AffineTransform.getTranslateInstance((.5f - .5f * scaleX) + x + off_x,
					.5f + .5f * scaleY + y + off_y);
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
