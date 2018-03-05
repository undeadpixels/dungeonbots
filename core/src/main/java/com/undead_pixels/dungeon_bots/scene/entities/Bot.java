package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.awt.geom.Point2D;


/**
 * An Actor intended to be scripted and controlled by player users in a code
 * REPL or Editor
 * 
 * @author Stewart Charles, Kevin Parker
 * @version 1.0
 */
public class Bot extends RpgActor {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	protected String defaultCode;

	/**
	 * Constructor
	 * 
	 * @param world
	 *            The world this player belongs to
	 * @param name
	 *            The name of this player
	 */
	public Bot(World world, String name) {
		super(world, name, AssetManager.getTextureRegion("DawnLike/Characters/Player0.png", 7, 1));
		steps = 0;
		bumps = 0;

		//world.getDefaultWhitelist().addAutoLevelsForBindables(this);
	}

	public void setPosition(Point2D.Float v) {
		world.getTile(this.getPosition()).setOccupiedBy(null);
		sprite.setX(v.x);
		sprite.setY(v.y);
		world.getTile(this.getPosition()).setOccupiedBy(this);
	}

	public int getSteps() {
		return steps;
	}

	public TeamFlavor getTeam() {
		return TeamFlavor.PLAYER;
	}

}
