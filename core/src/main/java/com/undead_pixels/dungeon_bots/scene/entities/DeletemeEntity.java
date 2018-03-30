/**
 * 
 */
package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;

/**
 * This needs to be refactored out by just creating new entity classes for each entity created using this class
 */
public class DeletemeEntity extends RpgActor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param world
	 * @param name
	 * @param tex
	 * @param scripts
	 * @param x
	 * @param y
	 */
	public DeletemeEntity(World world, TextureRegion tex, float x, float y) {
		super(world, "deleteme", tex, new UserScriptCollection(), x, y);
	}

	@Override
	public String inspect() {
		return "How can you see this?";
	}
}
