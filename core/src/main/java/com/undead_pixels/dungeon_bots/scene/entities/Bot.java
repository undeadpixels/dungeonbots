package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
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
	
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Characters/Player0.png", 7, 1);

	@Deprecated
	protected String defaultCode;

	/**
	 * Constructor
	 * 
	 * @param world	The world this player belongs to
	 * @param name	The name of this player
	 */
	public Bot(World world, String name) {
		this(world, name, 0, 0);
	}

	/**
	 * @param world
	 * @param string
	 * @param x
	 * @param y
	 */
	public Bot(World world, String name, float x, float y) {
		super(world, name, DEFAULT_TEXTURE, world.getPlayerTeamScripts(), x, y);
		steps = 0;
		bumps = 0;
	}

	/**
	 * Set the position of this bot
	 * 
	 * @param pos
	 */
	public void setPosition(Point2D.Float pos) {
		world.getTile(this.getPosition()).setOccupiedBy(null);
		sprite.setX(pos.x);
		sprite.setY(pos.y);
		world.getTile(this.getPosition()).setOccupiedBy(this);
	}

	/**
	 * Return the number of steps this Bot has taken
	 * 
	 * @return
	 */
	public int getSteps() {
		return steps;
	}

	public TeamFlavor getTeam() {
		return TeamFlavor.PLAYER;
	}

	@Override
	public Boolean canTake() {
		return true;
	}

}
