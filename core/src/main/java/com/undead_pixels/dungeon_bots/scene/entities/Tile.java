package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;

/**
 * A tile in the terrain
 */
public class Tile extends SpriteEntity {


	/**
	 * @param world		The world to contain this Actor
	 * @param name
	 * @param script
	 * @param tex		A texture for this Actor
	 * @param x
	 * @param y
	 */
	public Tile(World world, String name, LuaSandbox script, TextureRegion tex, float x, float y) {
		super(world, name, script, tex, x, y);
		// TODO Auto-generated constructor stub
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getId() {
		return id;
	}
}
