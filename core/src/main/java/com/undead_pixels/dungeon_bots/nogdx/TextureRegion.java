package com.undead_pixels.dungeon_bots.nogdx;

public class TextureRegion {
	
	private final Texture tex;
	
	private final int x, y, w, h;

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getW() {
		return w;
	}

	public int getH() {
		return h;
	}
	
	public int getX2() {
		return x + w;
	}
	
	public int getY2() {
		return y + h;
	}

	public Texture getTex() {
		return tex;
	}

	public TextureRegion(Texture tex, int x, int y, int w, int h) {
		super();
		this.tex = tex;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
}
