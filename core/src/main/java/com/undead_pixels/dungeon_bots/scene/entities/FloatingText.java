package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.undead_pixels.dungeon_bots.nogdx.SpriteBatch;

public class FloatingText extends ChildEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Font font = new Font("Arial", Font.BOLD, 12);
	private static final float fontSize = 12.0f;
	
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
	
	
	private LinkedList<TextInfo> lines = new LinkedList<>();

	public FloatingText(Entity parent, String name) {
		super(parent, name);
	}
	
	public void addLine(String line) {
		lines.add(new TextInfo(line));
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
	public void render(SpriteBatch batch) {
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

	public float getDuration() {
		return duration;
	}

	public void setDuration(float duration) {
		if(duration < 0) {
			duration = 0;
		}
		if(fadeDuration < duration) {
			fadeDuration = duration;
		}
		
		this.duration = duration;
	}

	public float getFadeDuration() {
		return fadeDuration;
	}

	public void setFadeDuration(float fadeDuration) {
		if(fadeDuration < 0) {
			fadeDuration = 0;
		}
		if(fadeDuration < duration) {
			fadeDuration = duration;
		}
		this.fadeDuration = fadeDuration;
	}
	
	private class TextInfo {
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
		
		private Point2D.Float render(SpriteBatch batch, Point2D.Float pos) {
			float fadeRatio = (duration - age) / fadeDuration;
			if(fadeRatio > 1) {
				fadeRatio = 1;
			}
			if(font != null) {
				
				float scale = .75f / fontSize;
				
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
