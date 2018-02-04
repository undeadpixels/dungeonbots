package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.Color;
import java.awt.Graphics2D;

public class SpriteBatch {
	
	private final Graphics2D g;
	private final float w;
	private final float h;
	private Color clearColor = Color.red;

	public SpriteBatch(Graphics2D g2d, float w, float h) {
		this.g = g2d;
		this.w = w;
		this.h=h;
	}

	public void begin() {
	}

	public void end() {
	}

	public void glClearColor(float r, float g, float b, float a) {
		this.glClearColor(new Color(r, g, b, a));
	}

	public void glClearColor(Color color) {
		clearColor = color;
	}

	public void glClear() {
		g.setColor(clearColor);
		g.fillRect(0, 0, (int)w, (int)h); // TODO
	}

	public void draw(TextureRegion backgroundImage, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	public void setProjectionMatrix(OrthographicCamera cam) {
		// TODO Auto-generated method stub
		
	}

}
