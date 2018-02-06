package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import com.undead_pixels.dungeon_bots.math.Vector2;

/**
 * A camera class that allows transformations and various other things
 */
public class OrthographicCamera {

	/**
	 * Zoom factor (size of each tile compared to the viewport)
	 */
	private float zoom;
	
	/**
	 * Aspect ratio of the content
	 */
	private float aspectRatio;
	
	/**
	 * Camera center position
	 */
	private Point2D.Float position;
	
	/**
	 * Size of the viewport
	 */
	private float viewportWidth, viewportHeight;

	/**
	 * Constructor
	 * 
	 * @param w	The width of the viewport
	 * @param h	The height of the viewport
	 */
	public OrthographicCamera(float w, float h) {
		viewportWidth = w;
		viewportHeight = h;
	}

	@Deprecated
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Deprecated
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

	/**
	 * Performs the inverse transform of this camera on the given point
	 * 
	 * @param pt		The point to un-project
	 * @return		An un-projected point
	 */
	public Point2D.Float unproject(Point2D.Float pt) {
		AffineTransform xform = getTransform();
		Point2D.Float ret = new Point2D.Float();
		try {
			xform.inverseTransform(pt, ret);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * The only useful output of a camera
	 * 
	 * @return	The current transform of the camera
	 */
	public AffineTransform getTransform() {
		float size = Math.min(viewportWidth / aspectRatio, viewportHeight);
		float scale = size * zoom;
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
		
		aspectRatio = size.x / size.y;
		float ratioW = Math.max(viewportWidth / viewportHeight, 1) / size.x;
		float ratioH = Math.max(viewportHeight / viewportWidth, 1) / size.y;
		if(ratioW > ratioH) {
			zoom = ratioW;
		} else {
			zoom = ratioH;
		}
		position = new Point2D.Float(size.x/2, size.y/2);
	}

	@Deprecated
	public void zoomFor(Vector2 size) {
		zoomFor(new Point2D.Float(size.x, size.y));
	}

	/**
	 * Notifies the camera of a new viewport size
	 * 
	 * @param w	Width
	 * @param h	Height
	 */
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

}