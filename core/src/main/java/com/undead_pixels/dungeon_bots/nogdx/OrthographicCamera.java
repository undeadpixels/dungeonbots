package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import com.undead_pixels.dungeon_bots.math.Vector2;

public class OrthographicCamera {

	public float zoom;
	public Vector2 position;
	public float viewportWidth, viewportHeight;

	public OrthographicCamera(float w, float h) {
		viewportWidth = w;
		viewportHeight = h;
	}

	public void update() {
		// TODO Auto-generated method stub
		
	}

	public Vector2 unproject(Vector2 pt) {
		AffineTransform xform = getTransform();
		Point2D.Float ret = new Point2D.Float();
		try {
			xform.inverseTransform(new Point2D.Float(pt.x, pt.y), ret);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return new Vector2(ret.x, ret.y);
	}

	public AffineTransform getTransform() {
		float size = Math.min(viewportWidth, viewportHeight);
		AffineTransform ret = AffineTransform.getScaleInstance(size * zoom, size * zoom);
		//ret.translate(0, ty);
		
		System.out.println(viewportWidth +", "+viewportHeight+", "+zoom);
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				Point2D src = new Point2D.Double(j, i);
				System.out.print(ret.transform(src, null) + "  ");
			}
			System.out.println();
		}
		return ret;
	}

	public void zoomFor(Vector2 size) {
		
		float ratioW = Math.max(viewportWidth / viewportHeight, 1) / size.x;
		float ratioH = Math.max(viewportHeight / viewportWidth, 1) / size.y;
		if(ratioW < ratioH) {
			zoom = ratioW;
		} else {
			zoom = ratioH;
		}
		position = new Vector2(size.x/2, size.y/2);
	}

}
