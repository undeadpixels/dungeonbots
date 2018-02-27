package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.geom.Point2D;

import com.undead_pixels.dungeon_bots.nogdx.Sprite;
import com.undead_pixels.dungeon_bots.nogdx.SpriteBatch;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;

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
		this(world, name, tex, 0, 0);
	}

	/**
	 * Constructor for a SpriteEntity
	 * 
	 * @param world	The world
	 * @param name	This entity's name
	 * @param tex	The texture to show
	 * @param x		Location X, in tiles
	 * @param y		Location Y, in tiles
	 */
	public SpriteEntity(World world, String name, TextureRegion tex, float x, float y) {
		super(world, name);
		if(tex == null)
			sprite = new Sprite();
		else {
			sprite = new Sprite(tex);
		}
		sprite.setPosition(x, y);
	}

	@Override
	public void update(float dt) {
		super.update(dt);
	}

	@Override
	public void render(SpriteBatch batch) {
		if(sprite != null && sprite.getTexture() != null) {
			//sprite.setRotation((float) ((System.currentTimeMillis() % 5000l) * (360f / 5000)));
			//System.out.println("Rendering sprite "+name+" @ "+getPosition() + ", scale = " + sprite.getScaleX()+", origin = "+sprite.getOriginX());
			sprite.draw(batch);
		}		
	}

	@Override
	public Point2D.Float getPosition() {
		return sprite.getPosition();
	}
	
	@Override
	public float getScale() {
		return sprite.getScaleX();
	}

}
