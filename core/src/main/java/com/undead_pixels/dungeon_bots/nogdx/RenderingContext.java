package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Used for rendering of the game screen.
 * 
 * Includes a matrix transform, a Graphics2D reference, and some utilities for drawing to a WorldView.
 */
public class RenderingContext {

	/**
	 * The backing graphics reference
	 */
	private final Graphics2D g;

	/**
	 * Width and height of the drawable area
	 */
	private final float w, h;

	/**
	 * The background color
	 */
	private Color clearColor = Color.red;

	/**
	 * The matrix to apply after the sprite's matrix is applied
	 */
	private AffineTransform projection;

	/**
	 * Constructor
	 * 
	 * @param g2d	A graphics context
	 * @param w		Width of the context
	 * @param h		Height of the context
	 */
	public RenderingContext(Graphics2D g2d, float w, float h) {
		this.g = g2d;
		this.w = w;
		this.h = h;
	}

	/**
	 * Sets the clear color
	 * 
	 * @param r		Red
	 * @param g		Green
	 * @param b		Blue
	 * @param a		Alpha
	 */
	public void setClearColor(float r, float g, float b, float a) {
		this.setClearColor(new Color(r, g, b, a));
	}

	/**
	 * Sets the clear color
	 * 
	 * @param color	The (background) color
	 */
	public void setClearColor(Color color) {
		clearColor = color;
	}

	/**
	 * Clears anything showing on the given context (sets it to the current
	 * clear color)
	 */
	public void clearContext() {
		g.setColor(clearColor);
		g.fillRect(0, 0, (int) w, (int) h);
	}

	/**
	 * Draws a TextureRegion
	 * 
	 * Not entirely functional yet
	 * 
	 * @param img	The image
	 * @param x		The X coordinate
	 * @param y		The Y coordinate
	 */
	public void draw(TextureRegion img, int x, int y) {
		draw(img, AffineTransform.getTranslateInstance(x, y));
		// TODO - this isn't actually good enough
	}

	/**
	 * Draws a TextureRegion, given a transform
	 * 
	 * @param img		The texture
	 * @param xform		The transform
	 */
	public void draw(TextureRegion img, AffineTransform xform) {
		AffineTransform totalTransform = new AffineTransform(xform);
		totalTransform.preConcatenate(projection);

		g.setTransform(totalTransform);

		g.drawImage(img.getTex().getImg(), 0, 0, img.getW(), img.getH(), img.getX(), img.getY(), img.getX2(),
				img.getY2(), null);

	}

	/**
	 * Updates this RenderingContext's projection matrix from a camera
	 * 
	 * @param cam	The camera
	 */
	public void setProjectionMatrix(OrthographicCamera cam) {
		this.projection = cam.getTransform();
	}

	/**
	 * Draws a string in this RenderingContext
	 * 
	 * @param text	The string to draw
	 * @param font	The font to use
	 * @param color	The color
	 * @param xform	The transform to apply before drawing
	 */
	public void drawString(String text, Font font, Color color, AffineTransform xform) {
		AffineTransform totalTransform = new AffineTransform(xform);
		totalTransform.preConcatenate(projection);

		g.setTransform(totalTransform);
		g.setColor(color);
		g.setFont(font);

		g.drawString(text, 0, 0);

	}

	/**
	 * Draws a line
	 * 
	 * @param x1		X coordinate of the first point
	 * @param y1		Y coordinate of the first point
	 * @param x2		X coordinate of the second point
	 * @param y2		Y coordinate of the second point
	 */
	public void drawLine(float x1, float y1, float x2, float y2) {
		g.setTransform(new AffineTransform());

		Point2D t1 = projection.transform(new Point2D.Float(x1, y1), new Point2D.Float());
		Point2D t2 = projection.transform(new Point2D.Float(x2, y2), new Point2D.Float());

		int x1t = (int) t1.getX();
		int y1t = (int) t1.getY();
		int x2t = (int) t2.getX();
		int y2t = (int) t2.getY();

		g.drawLine(x1t, y1t, x2t, y2t);
	}

	/**
	 * Draws a rectangle 
	 * 
	 * @param x			Left x value
	 * @param y			Top y value
	 * @param width		width
	 * @param height		height
	 */
	public void drawRect(float x, float y, float width, float height) {
		g.setTransform(new AffineTransform());

		// Note: project.getScaleY() will return a negative number due to the
		// presentation flip.
		Point2D pt = projection.transform(new Point2D.Float(x, y), null);

		int x_i = (int) pt.getX();
		int y_i = (int) pt.getY();

		int width_i = (int) (width * projection.getScaleX());
		int height_i = -(int) (height * projection.getScaleY());

		g.drawRect(x_i, y_i, width_i, height_i);
	}

	/**
	 * Fills a rectangle 
	 * 
	 * @param x			Left x value
	 * @param y			Top y value
	 * @param width		width
	 * @param height		height
	 */
	public void fillRect(float x, float y, float width, float height) {
		g.setTransform(new AffineTransform());

		// Note: project.getScaleY() will return a negative number due to the
		// presentation flip.
		Point2D pt = projection.transform(new Point2D.Float(x, y), null);

		int x_i = (int) pt.getX();
		int y_i = (int) pt.getY();

		int width_i = (int) (width * projection.getScaleX());
		int height_i = -(int) (height * projection.getScaleY());

		g.fillRect(x_i, y_i, width_i, height_i);
	}

}
