package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;

public class Switch extends SpriteEntity implements Useable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Switch(World world, String name, TextureRegion tex) {
		super(world, name, tex, new UserScriptCollection());
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	@Override
	public float getZ() {
		return 0;
	}

	@Override
	public Boolean use() {
		return null;
	}
}
