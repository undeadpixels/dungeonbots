package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class AnimationSparkle extends Animation {

	private final Point2D.Float[] points;
	private static final Color[] colors = new Color[] { Color.white, Color.cyan,  Color.YELLOW };


	public AnimationSparkle() {
		this(10, 0, 0, 1, 1);
	}


	public AnimationSparkle(int sparkles, float x, float y, float width, float height) {
		super(5f);
		points = new Point2D.Float[sparkles];
		for (int i = 0; i < points.length; i++) {
			Point2D.Float pt = new Point2D.Float((float) Math.random() * width + x, (float) Math.random() * height + y);
			points[i] = pt;
		}
	}


	private static final float scalar = 4;


	@Override
	public void drawFrame(RenderingContext batch, TextureRegion tex, float x, float y, float rotation, float scaleX,
			float scaleY, float dt) {
		if (tex == null)
			return;

		AffineTransform xform;
		if (rotation == 0) {
			xform = AffineTransform.getTranslateInstance((.5f - .5f * scaleX) + x, .5f + .5f * scaleY + y);
			xform.scale(scaleX / tex.getW(), -scaleY / tex.getH());
		} else {
			xform = AffineTransform.getTranslateInstance(.5 + x, .5 * scaleY + y);
			xform.rotate(rotation);
			xform.scale(scaleX / tex.getW(), -scaleY / tex.getH());
			xform.translate(-.5, .5);
		}
		Graphics2D g = batch.draw(tex, xform);
		float width = scaleX * tex.getW();
		float height = scaleY * tex.getH();

		float lifeTime = this.getLifetime();
		int focusIdx = (int) (lifeTime * (points.length + 20));
		for (int i = 0; i < points.length; i++) {
			float sparkleSize = scalar;
			if (focusIdx != i)
				sparkleSize /= (Math.abs(focusIdx - i));
			if (sparkleSize < (scalar / 2))
				continue;
			float ptX = points[i].x * width, ptY = points[i].y * height;
			g.setColor(colors[i % colors.length]);
			g.drawLine((int) (ptX + sparkleSize), (int) ptY, (int) (ptX - sparkleSize), (int) ptY);
			g.drawLine((int) ptX, (int) (ptY - sparkleSize), (int) ptX, (int) (ptY + sparkleSize));
		}

	}

}
