package com.undead_pixels.dungeon_bots.scene;

import java.io.Serializable;

/**
 * Enum of team types
 */
public enum TeamFlavor implements Serializable {
	/**
	 * The player and their bots
	 */
	PLAYER,
	
	/**
	 * The enemy and anything they own
	 */
	ENEMY,
	
	/**
	 * No team (such as terrain)
	 */
	NONE
}
