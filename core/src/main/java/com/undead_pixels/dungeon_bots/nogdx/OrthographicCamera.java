package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class OrthographicCamera {

	/**
	 * Zoom factor (size of each tile compared to the viewport)
	 */
	private float zoom = 1.0f;
	
	/**
	 * The size of map to always show
	 */
	private Point2D.Float mapSize;
	
	/**
	 * Camera center position
	 */
	private Point2D.Float position;
	
	/**
	 * Size of the viewport
	 */
	private float viewportWidth, viewportHeight;

	public OrthographicCamera(float w, float h) {
		viewportWidth = w;
		viewportHeight = h;
	}

	@Deprecated
	public void update() {
		// TODO Auto-generated method stub
		
	}

	public Point2D.Float unproject(Point2D.Float pt) {
		AffineTransform xform = getTransform();
		Point2D.Float ret = new Point2D.Float();
		try {
			xform.inverseTransform(new Point2D.Float(pt.x, pt.y), ret);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return new Point2D.Float(ret.x, ret.y);
	}

	public AffineTransform getTransform() {
		
		float maxPixelsPerTileX = viewportWidth / mapSize.x;
		float maxPixelsPerTileY = viewportHeight / mapSize.y;
		float maxPixelsPerTile  = Math.min(maxPixelsPerTileX, maxPixelsPerTileY);
		
		float scale = maxPixelsPerTile * zoom;
		AffineTransform ret = AffineTransform.getScaleInstance(scale, -scale);
		ret.translate(viewportWidth/2/scale - position.x, -viewportHeight/2/scale - position.y);
		//ret.translate(0, ty);
		return ret;
	}

	/**
	 * Sets up the zoom of this camera to fit a map of a given size
	 * 
	 * @param size	Size of a map
	 */
	public void zoomFor(Point2D.Float size) {
		mapSize = size;
		
		zoom = 1f;
		
		position = new Point2D.Float(size.x/2, size.y/2);
	}

	public void setViewportSize(float w, float h) {
		viewportWidth = w;
		viewportHeight = h;
	}

	public float getZoom() {
		return zoom;
	}
	
	public void setZoom(float newZoom){
		zoom = newZoom;
	}
	
	public void setZoomOnMinMaxRange(float newZoom) {
		float leftThing = (float) Math.log(getMinZoom());
		float rightThing = (float) Math.log(getMaxZoom());
		float zoomT = leftThing*(1-newZoom) + rightThing*newZoom;
		setZoom((float)Math.exp(zoomT));
	}

	public float getMinZoom() {
		return 0.25f;
	}

	public float getMaxZoom() {
		return 4.0f;
	}

}
