package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;


/**
 * A texture along with transform information
 */
public class Sprite implements Serializable {
	
	/**
	 * Location of the bottom-left corner
	 */
	private float x, y;
	
	/**
	 * Scale of this sprite, applied from the center
	 */
	private float scaleX = 1f, scaleY = 1f;
	
	/**
	 * Rotation, applied from the center
	 */
	private float rotation;
	
	/**
	 * This sprite's texture
	 */
	private TextureRegion tex;

	/**
	 * Constructor
	 * 
	 * Location is set to (0, 0), scale is set to 1, and rotation is 0.
	 * 
	 * @param tex	The texture
	 */
	public Sprite(TextureRegion tex) {
		this.tex = tex;
	}

	/**
	 * Constructor
	 * 
	 * Location is set to (0, 0), scale is set to 1, and rotation is 0.
	 * 
	 * No texture is attached.
	 */
	public Sprite() {
	}

	/**
	 * @param x	This sprite's new X coordinate
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @param y	This sprite's new Y coordinate
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * @return	This sprite's X coordinate
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return	This sprite's Y coordinate
	 */
	public float getY() {
		return y;
	}

	/**
	 * Sets the position of this sprite
	 * 
	 * @param x	This sprite's new X coordinate
	 * @param y	This sprite's new Y coordinate
	 */
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		
	}

	/**
	 * @return	This sprite's current TextureRegion
	 */
	public TextureRegion getTexture() {
		return tex;
	}

	/**
	 * @param texture	The new texture
	 */
	public void setTexture(TextureRegion texture) {
		this.tex = texture;
	}

	/**
	 * Draws this sprite into a given SpriteBatch
	 * 
	 * @param batch	A SpriteBatch object to draw into
	 */
	public void draw(RenderingContext batch) {
		if(tex != null) {
			AffineTransform xform;
			if(rotation == 0) {
				xform = AffineTransform.getTranslateInstance((.5f-.5f*scaleX) + x, .5f + .5f*scaleY+y);
				xform.scale(scaleX / tex.getW(), -scaleY / tex.getH());
			} else {
				xform = AffineTransform.getTranslateInstance(.5 + x, .5*scaleY + y);
				xform.rotate(rotation);
				xform.scale(scaleX / tex.getW(), -scaleY / tex.getH());
				xform.translate(-.5, .5);
			}
			batch.draw(tex, xform);			
		}
	}

	/**
	 * Sets the scale of this sprite
	 * 
	 * @param sx		New scale in X
	 * @param sy		New scale in Y
	 */
	public void setScale(float sx, float sy) {
		this.scaleX = sx;
		this.scaleY = sy;
		
	}
	
	/**
	 * @param s	The new scale of this sprite (both X and Y)
	 */
	public void setScale(float s) {
		setScale(s, s);
	}
	
	/**
	 * @return	The current scale of this sprite in X
	 */
	public float getScaleX() {
		return scaleX;
	}

	/**
	 * @return The current scale of this sprite in Y
	 */
	public float getScaleY() {
		return scaleY;
	}

	/**
	 * @param r	The new rotation in radians
	 */
	public void setRotation(float r) {
		this.rotation = r;
	}

	/**
	 * @return	The current rotation in radians
	 */
	public float getRotation() {
		return rotation;
	}

	/**
	 * @return	The current position of this sprite
	 */
	public Point2D.Float getPosition() {
		return new Point2D.Float(x, y);
	}

}
