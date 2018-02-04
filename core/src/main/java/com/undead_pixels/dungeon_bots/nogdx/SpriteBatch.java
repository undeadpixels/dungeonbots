package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class SpriteBatch {
	
	private final Graphics2D g;
	private final float w;
	private final float h;
	private Color clearColor = Color.red;
	private AffineTransform projection;

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

	public void draw(TextureRegion img, int x, int y) {
		draw(img, AffineTransform.getTranslateInstance(x, y));
		// TODO - this isn't acutally good enough
	}
	
	public void draw(TextureRegion img, AffineTransform xform) {
		AffineTransform totalTransform = new AffineTransform(xform);
		totalTransform.preConcatenate(projection);
		
		g.setTransform(totalTransform);
		
		//System.out.println("g.xform = "+projection);
		//System.out.println("sprite.xform = "+xform);
		//System.out.println("combined.xform = "+g.getTransform());
		//System.out.println("before -> "+g.getTransform().transform(new Point2D.Double(), null));
		// TODO - draw
		
		//System.out.println(g.getTransform().transform(new Point2D.Double(), null));
		g.drawImage(img.getTex().getImg(), 0, 0, img.getW(), img.getH(),
				img.getX(), img.getY(), img.getX2(), img.getY2(),
				null);
		
	}

	public void setProjectionMatrix(OrthographicCamera cam) {
		this.projection = cam.getTransform();
		//System.out.println("Setting transform: "+cam.getTransform());
		//g.transform(cam.getTransform());
	}

}
