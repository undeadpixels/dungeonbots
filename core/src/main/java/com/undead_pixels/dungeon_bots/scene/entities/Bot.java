package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.scene.GetState;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.actions.Action;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.security.SecurityContext;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.awt.geom.Point2D;

import org.luaj.vm2.LuaValue;

/**
 * An Actor intended to be scripted and controlled by player users in a code
 * REPL or Editor
 * 
 * @author Stewart Charles, Kevin Parker
 * @version 1.0
 */
public class Bot extends RpgActor {

	@Deprecated
	protected String defaultCode;

	/**
	 * Constructor
	 * 
	 * @param world	The world this player belongs to
	 * @param name	The name of this player
	 */
	public Bot(World world, String name) {
		super(world, name, AssetManager.getTextureRegion("DawnLike/Characters/Player0.png", 7, 1));
		steps = 0;
		bumps = 0;

		//world.getDefaultWhitelist().addAutoLevelsForBindables(this);
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

}
