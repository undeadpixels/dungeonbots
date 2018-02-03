package com.undead_pixels.dungeon_bots.scene.entities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class FloatingText extends ChildEntity {
	
	private static BitmapFont font;
	private static final float fontSize;
	
	static {

		if(Gdx.files == null) {
			font = null;
			fontSize = 0.0f;
		} else {
			font = new BitmapFont();
			fontSize = font.getLineHeight();
		}
	}
	
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
		Vector2 pos = getPosition();
		pos = new Vector2(pos.x, pos.y + .2f);
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
		private Color color = Color.WHITE;
		
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
		
		private Vector2 render(SpriteBatch batch, Vector2 pos) {
			float fadeRatio = (duration - age) / fadeDuration;
			if(fadeRatio > 1) {
				fadeRatio = 1;
			}
			if(font != null) {
				color.a = fadeRatio;
				font.setColor(color);
				font.setUseIntegerPositions(false);
				font.getData().setScale(0.75f / fontSize);
				GlyphLayout layout = new GlyphLayout(font, text);

				font.draw(batch, layout, pos.x - layout.width/2 + .5f, pos.y + layout.height + 1);
				return new Vector2(pos.x, pos.y + font.getLineHeight());
			} else {
				return pos;
			}
		}
	}

}
