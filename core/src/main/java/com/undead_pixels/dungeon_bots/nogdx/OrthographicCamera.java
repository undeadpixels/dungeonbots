package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;
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

	public Vector2 unproject(Vector2 vector2) {
		// TODO Auto-generated method stub
		return null;
	}

	public AffineTransform getTransform() {
		AffineTransform ret = AffineTransform.getScaleInstance(viewportWidth * zoom * 2, viewportHeight * zoom * 2);
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

}
