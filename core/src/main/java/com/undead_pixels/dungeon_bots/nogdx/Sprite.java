package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.geom.AffineTransform;

public class Sprite {
	
	private float x, y;
	
	private float scaleX, scaleY;
	
	private float rotation;
	
	private TextureRegion tex;

	public Sprite(TextureRegion tex) {
		this.tex = tex;

		//setSize(1.0f, 1.0f);
		//setOrigin(.5f, .5f);
	}

	public Sprite() {
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		
	}

	public TextureRegion getTexture() {
		return tex;
	}

	public void draw(SpriteBatch batch) {
		AffineTransform xform;
		if(rotation == 0) {
			xform = AffineTransform.getTranslateInstance(-.5 + x, -.5 + y);
		} else {
			xform = AffineTransform.getTranslateInstance(x, y);
			xform.rotate(rotation);
			xform.translate(-.5, -.5);
		}
		xform.scale(1.0 / tex.getW(), -1.0 / tex.getH());
		batch.draw(tex, xform);
	}

	public void setScale(float sx, float sy) {
		this.scaleX = sx;
		this.scaleY = sy;
		
	}
	public void setScale(float s) {
		setScale(s, s);
	}
	
	public float getScaleX() {
		return scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	/**
	 * @param r	Rotation in radians
	 */
	public void setRotation(float r) {
		this.rotation = r;
	}

	public float getRotation() {
		return rotation;
	}

}
