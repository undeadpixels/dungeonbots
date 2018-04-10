package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;

public class HealthBar extends ChildEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HealthBar(Entity parent, String name) {
		super(parent, name);
	}

	@Override
	public void render(RenderingContext batch) {

	}

	@Override
	public float getZ() {
		return 100;
	}

	@Override
	public String inspect() {
		return "Some kind of mystical health bar?";
	}
}
