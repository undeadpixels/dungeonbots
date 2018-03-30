/**
 * 
 */
package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

/**
 * @author kevin
 *
 */
public class Goal extends SpriteEntity {
	
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Door0.png", 3, 5);

	/**
	 * @param world
	 * @param name
	 * @param x
	 * @param y
	 */
	public Goal(World world, String name, float x, float y) {
		super(world, name, DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
	}

	/* (non-Javadoc)
	 * @see com.undead_pixels.dungeon_bots.scene.BatchRenderable#getZ()
	 */
	@Override
	public float getZ () {
		return 1;
	}

	/* (non-Javadoc)
	 * @see com.undead_pixels.dungeon_bots.scene.entities.Entity#isSolid()
	 */
	@Override
	public boolean isSolid () {
		return false;
	}
	
}
