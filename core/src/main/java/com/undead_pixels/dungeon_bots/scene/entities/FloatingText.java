package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;

public class FloatingText extends ChildEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The font of this text
	 */
	private static Font font = new Font("Arial", Font.BOLD, 12);
	
	/**
	 * The font size of this text
	 */
	private static final float invFontSize = 20.0f;
	
	/**
	 * Amount of time that the text will continue to float before disappearing
	 * Includes the fade-out time.
	 */
	private float duration = 8.0f;

	/**
	 * Amount of time that text takes to fade from full opacity to invisible
	 * Included in the total duration
	 */
	private float fadeDuration = 2.0f;
	
	
	/**
	 * Internal storage
	 */
	private LinkedList<TextInfo> lines = new LinkedList<>();

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param name
	 */
	public FloatingText(Entity parent, String name) {
		super(parent, name);
	}
	
	/**
	 * Add a line of text
	 * 
	 * @param line
	 */
	public void addLine(String line) {
		for(String actualLine : line.split("\n")) {
			lines.add(new TextInfo(actualLine));
		}
	}
	
	@Override
	public void update(float dt) {
		for(ListIterator<TextInfo> iter = lines.listIterator(); iter.hasNext(); ) {
			TextInfo i = iter.next();
			i.update(dt);
			if(i.isDead()) {
				iter.remove();
			}
		}
	}

	@Override
	public void render(RenderingContext batch) {
		Point2D.Float pos = getPosition();
		pos = new Point2D.Float(pos.x, pos.y + .4f);
		for(Iterator<TextInfo> iter = lines.descendingIterator(); iter.hasNext(); ) {
			pos = iter.next().render(batch, pos);
		}
	}

	@Override
	public float getZ() {
		return 150;
	}

	/**
	 * @return	How long the given text is visible for
	 */
	public float getDuration() {
		return duration;
	}

	/**
	 * @param duration	How long the given text is visible for
	 */
	public void setDuration(float duration) {
		if(duration < 0) {
			duration = 0;
		}
		if(fadeDuration < duration) {
			fadeDuration = duration;
		}
		
		this.duration = duration;
	}

	/**
	 * @return	How long the fade animation will last
	 */
	public float getFadeDuration() {
		return fadeDuration;
	}

	/**
	 * @param fadeDuration	How long the fade animation will last
	 */
	public void setFadeDuration(float fadeDuration) {
		if(fadeDuration < 0) {
			fadeDuration = 0;
		}
		if(fadeDuration < duration) {
			fadeDuration = duration;
		}
		this.fadeDuration = fadeDuration;
	}

	@Override
	public String inspect() {
		return "What is this?";
	}

	/**
	 * Internal info/rendering about text
	 */
	private class TextInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private String text;
		private Color color = Color.white;
		
		private float age;
		
		public TextInfo(String text) {
			this.text = text;
		}
		
		private boolean isDead() {
			return age >= duration;
		}
		
		private void update(float dt) {
			age += dt;
		}
		
		private Point2D.Float render(RenderingContext batch, Point2D.Float pos) {
			float fadeRatio = (duration - age) / fadeDuration;
			if(fadeRatio > 1) {
				fadeRatio = 1;
			}
			if(font != null) {
				
				float scale = .75f / invFontSize;
				
				Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255*fadeRatio));
				
				FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
				Rectangle2D bounds = font.getStringBounds(text, frc);

				
				AffineTransform xform = AffineTransform.getScaleInstance(scale, -scale);
				//xform.preConcatenate(AffineTransform.getTranslateInstance(bounds.getX() - bounds.getWidth()/2, 0));
				xform.preConcatenate(AffineTransform.getTranslateInstance(bounds.getX()*scale - bounds.getWidth()/2*scale + .5f, -bounds.getY() * scale));
				xform.preConcatenate(AffineTransform.getTranslateInstance(pos.x, pos.y));
				
				batch.drawString(text, font, c, xform);
				
				return new Point2D.Float(pos.x, (float) (pos.y + bounds.getHeight()*scale));
			} else {
				return pos;
			}
		}
	}

}
