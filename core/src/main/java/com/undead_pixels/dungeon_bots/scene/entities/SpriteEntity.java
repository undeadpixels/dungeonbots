package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.Image;
import java.awt.geom.Point2D;

import com.undead_pixels.dungeon_bots.nogdx.Sprite;
import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

/**
 * A regular Entity that is based upon a Sprite, not some other form of graphic
 */
public abstract class SpriteEntity extends Entity implements HasImage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The sprite for this Entity. Also keeps track of its location.
	 */
	protected Sprite sprite;


	/**
	 * @param world		The world to contain this Actor
	 * @param tex		A texture for this Actor. May be null.
	 */
	public SpriteEntity(World world, String name, TextureRegion tex, UserScriptCollection scripts) {
		this(world, name, tex, scripts, 0, 0);
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
	public SpriteEntity(World world, String name, TextureRegion tex, UserScriptCollection scripts, float x, float y) {
		super(world, name, scripts);
		if (tex == null)
			sprite = new Sprite();
		else {
			sprite = new Sprite(tex);
		}
		sprite.setPosition(x, y);
	}


	@Override
	public void update(float dt) {
		if (sprite != null)
			sprite.sinceRender += dt;
		super.update(dt);
	}


	@Override
	public void render(RenderingContext batch) {
		if (sprite != null && sprite.getTexture() != null) {
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

	public void setPosition(float x, float y){
		this.sprite.setX(x);
		this.sprite.setY(y);
	}
	
	/**
	 * @param x
	 * @param y
	 */
	@Bind(value=SecurityLevel.AUTHOR,doc="Set the position of the entity")
	public void setPosition(
			@Doc("The X position of the entity") LuaValue x,
			@Doc("The Y position of the entity") LuaValue y) {
		this.sprite.setX(x.tofloat());
		this.sprite.setY(y.tofloat());
	}

	@Override
	/**Returns a new image associated with this SpriteEntity.*/
	public Image getImage() {
		return sprite.getTexture().toImage();
	}

	public Sprite getSprite() {
		return sprite;
	}

}
