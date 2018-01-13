package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaScript;

/**
 * A tile in the terrain
 */
public class Tile extends SpriteEntity {


	/**
	 * @param world		The world to contain this Actor
	 * @param tex		A texture for this Actor
	 */
	public Tile(World world, TextureRegion tex) {
		super(world, tex);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user script that is run on this object
	 * @param tex		A texture for this Actor
	 */
	public Tile(World world, LuaScript script, TextureRegion tex) {
		super(world, script, tex);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param sprite		A texture for this Actor
	 */
	public Tile(World world, Sprite sprite) {
		super(world, sprite);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param world		The world to contain this Actor
	 * @param script		A user script that is run on this object 
	 * @param sprite		A texture for this Actor
	 */
	public Tile(World world, LuaScript script, Sprite sprite) {
		super(world, script, sprite);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector2 getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(SpriteBatch batch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSolid() {
		// TODO Auto-generated method stub
		return false;
	}

}
