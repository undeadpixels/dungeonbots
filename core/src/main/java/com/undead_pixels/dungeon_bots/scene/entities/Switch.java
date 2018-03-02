package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Useable;

public class Switch extends SpriteEntity implements Useable {

	public Switch(World world, String name, TextureRegion tex) {
		super(world, name, tex);
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
