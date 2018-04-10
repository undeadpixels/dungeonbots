package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * A region of a Texture
 */
public class TextureRegion implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The internal Texture
	 */
	private final Texture tex;
	
	/**
	 * Position and size of this region of this TextureRegion
	 */
	private final int x, y, w, h;
	
	private transient BufferedImage cachedImg;

	/**
	 * @return	The left X coordinate of this TextureRegion
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return	The top Y coordinate of this TextureRegion
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return	The width of this TextureRegion
	 */
	public int getW() {
		return w;
	}

	/**
	 * @return	The height of this TextureRegion
	 */
	public int getH() {
		return h;
	}

	/**
	 * @return	The right X coordinate of this TextureRegion
	 */
	public int getX2() {
		return x + w;
	}

	/**
	 * @return	The top Y coordinate of this TextureRegion
	 */
	public int getY2() {
		return y + h;
	}

	/**
	 * @return	The texture
	 */
	public Texture getTex() {
		return tex;
	}
	
	/**
	 * @return	A newly-allocated image representing this TextureRegion
	 * 			(without any of the surrounding texture cropped out by the region)
	 */
	public BufferedImage toImage() {
		if(cachedImg != null) {
			return cachedImg;
		}
		return cachedImg = tex.getImg().getSubimage(x, y, w, h);
	}

	/**
	 * Constructor
	 * 
	 * @param tex	Texture
	 * @param x		left X coordinate
	 * @param y		top Y coordinate
	 * @param w		width
	 * @param h		height
	 */
	public TextureRegion(Texture tex, int x, int y, int w, int h) {
		super();
		this.tex = tex;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	/**
	 * Creates a region within this texture region
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return		A new, smaller texture region
	 */
	public TextureRegion subregion(int x, int y, int w, int h) {
		return new TextureRegion(tex, this.x+x, this.y+y, w, h);
	}
	
}
