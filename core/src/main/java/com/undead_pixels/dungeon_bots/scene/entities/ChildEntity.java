package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.scene.World;

public abstract class ChildEntity extends Entity {
	
	/**
	 * The entity that this child attaches to
	 */
	protected Entity parent;

	public ChildEntity(Entity parent, String name) {
		super(parent.world, name);
		
		this.parent = parent;
	}

	@Override
	public Vector2 getPosition() {
		return parent.getPosition();
	}

	@Override
	public boolean isSolid() {
		return false;
	}
	
	@Override
	public float getScale() {
		return parent.getScale();
	}

}
