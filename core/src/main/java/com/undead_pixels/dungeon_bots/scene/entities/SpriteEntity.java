package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.Whitelist;

/**
 * A regular Entity that is based upon a Sprite, not some other form of graphic
 */
public abstract class SpriteEntity extends Entity {
	
	/**
	 * The sprite for this Entity. Also keeps track of its location.
	 */
	protected Sprite sprite;

	/**
	 * @param world		The world to contain this Actor
	 * @param tex		A texture for this Actor. May be null.
	 */
	public SpriteEntity(World world, String name, TextureRegion tex) {
		this(world, name, null, tex);
	}

	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user scriptEnv that is run on this object. May be null.
	 * @param tex		A texture for this Actor. May be null.
	 */
	public SpriteEntity(World world, String name, LuaSandbox script, TextureRegion tex) {
		this(world, name, script, tex, 0, 0);
	}

	public SpriteEntity(World world, String name, LuaSandbox script, TextureRegion tex, float x, float y) {
		super(world, name, script);
		if(tex == null) {
			sprite = new Sprite();
		} else {
			sprite = new Sprite(tex);
			sprite.setScale(1.0f/tex.getRegionWidth());
		}
		sprite.setPosition(x, y);
	}

	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user scriptEnv that is run on this object. May be null.
	 * @param tex		A texture for this Actor. May be null.
	 */
	public SpriteEntity(World world, String name, LuaSandbox script, TextureRegion tex, Whitelist whitelist) {
		super(world, name, script, whitelist);
		if(tex == null) {
			sprite = new Sprite();
		} else {
			sprite = new Sprite(tex);
		}
	}

	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(SpriteBatch batch) {
		if(sprite != null && sprite.getTexture() != null) {
			//System.out.println("Rendering sprite "+name+" @ "+getPosition() + ", scale = " + sprite.getScaleX());
			sprite.draw(batch);
		}
	}

	@Override
	public Vector2 getPosition() {
		return new Vector2(sprite.getX(), sprite.getY());
	}

}
