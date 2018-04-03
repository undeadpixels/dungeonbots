package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;

@Doc("A Switch is an Entity that's contextual use function invokes a event")
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
		// Signal appropriate observer to invoke onUse script if present
		return false;
	}

	@Override
	public String inspect() {
		return this.getClass().getSimpleName();
	}
}
