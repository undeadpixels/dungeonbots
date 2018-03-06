package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.Serializable;

public class OrthographicCamera implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

	/**
	 * Returns the described point in screen coordinates.
	 * 
	 * @param pt		An original point in world space
	 * @return		That point mapped to screen space
	 */
	public Point2D.Float unproject(Point2D.Float pt) {
		return unproject(pt.x, pt.y);
	}

	/**
	 * Returns the described point in screen coordinates.
	 * 
	 * @param x		An original point in world space
	 * @param y		An original point in world space
	 * @return		That point mapped to screen space
	 */
	public Point2D.Float unproject(float x, float y) {
		AffineTransform xform = getTransform();
		Point2D.Float ret = new Point2D.Float();
		try {
			xform.inverseTransform(new Point2D.Float(x, y), ret);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return new Point2D.Float(ret.x, ret.y);
	}

	/**
	 * @return	The current transform of this camera
	 */
	public AffineTransform getTransform() {

		float maxPixelsPerTileX = viewportWidth / mapSize.x;
		float maxPixelsPerTileY = viewportHeight / mapSize.y;
		float maxPixelsPerTile = Math.min(maxPixelsPerTileX, maxPixelsPerTileY);

		float scale = maxPixelsPerTile * zoom;
		AffineTransform ret = AffineTransform.getScaleInstance(scale, -scale);
		ret.translate(viewportWidth / 2 / scale - position.x, -viewportHeight / 2 / scale - position.y);
		// ret.translate(0, ty);
		return ret;
	}

	/**
	 * Sets up the zoom of this camera to fit a map of a given size
	 * 
	 * @param size		Size of a map
	 */
	public void zoomFor(Point2D.Float size) {
		mapSize = size;

		zoom = 1f;

		position = new Point2D.Float(size.x / 2, size.y / 2);
	}

	/**
	 * Notifies this camera of the size of the view it is outputting
	 * 
	 * @param w		width
	 * @param h		height
	 */
	public void setViewportSize(float w, float h) {
		viewportWidth = w;
		viewportHeight = h;
	}

	/**
	 * @return	The current zoom level
	 */
	public float getZoom() {
		return zoom;
	}

	/**
	 * @param newZoom	A new zoom level
	 */
	public void setZoom(float newZoom) {
		zoom = newZoom;
	}

	/**
	 * @param newZoom	A number between 0 and 1, indicating min to max zoom
	 */
	@Deprecated
	public void setZoomOnMinMaxRange(float newZoom) {
		setZoomInRange(getMinZoom(), newZoom, getMaxZoom());
	}
	
	/** Sets the zoom to a value that is a percentage between the given min and max.
	 * @param newZoom	A number between 0 and 1, indicating min to max zoom
	 */
	public void setZoomInRange(float min, float newZoom, float max){
		float leftThing = (float) Math.log(min);
		float rightThing = (float) Math.log(max);
		float zoomT = leftThing * (1 - newZoom) + rightThing * newZoom;
		setZoom((float) Math.exp(zoomT));
	}
	
	public float getZoomInRange(float min, float max){
		throw new RuntimeException("Not implemented yet.");
	}

	/**
	 * @return	Minimum allowable zoom
	 */
	@Deprecated
	public float getMinZoom() {
		return 0.25f;
	}

	/**
	 * @return	Maximum allowable zoom
	 */
	@Deprecated
	public float getMaxZoom() {
		return 4.0f;
	}

	/**
	 * @return	The central position of this camera
	 */
	public Point2D.Float getPosition() {
		return position;
	}

	/**
	 * Change where this camera is centered
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(float x, float y) {
		position = new Point2D.Float(x, y);
	}

}
