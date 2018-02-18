package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.geom.Point2D;

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
	public Point2D.Float getPosition() {
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
