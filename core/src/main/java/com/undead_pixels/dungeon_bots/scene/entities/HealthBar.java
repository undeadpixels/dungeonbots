package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.SpriteBatch;

public class HealthBar extends ChildEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HealthBar(Entity parent, String name) {
		super(parent, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void render(SpriteBatch batch) {
		// TODO Auto-generated method stub

	}

	@Override
	public float getZ() {
		return 100;
	}

}
