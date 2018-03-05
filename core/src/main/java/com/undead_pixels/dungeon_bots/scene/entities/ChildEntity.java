package com.undead_pixels.dungeon_bots.scene.entities;

import java.awt.geom.Point2D;

import com.undead_pixels.dungeon_bots.scene.TeamFlavor;

public abstract class ChildEntity extends Entity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The entity that this child attaches to
	 */
	protected Entity parent;

	public ChildEntity(Entity parent, String name) {
		super(parent.world, name, null);

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

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.NONE;
	}
}
