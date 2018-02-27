package com.undead_pixels.dungeon_bots.script.interfaces;

import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;

public interface HasTeam {

	/**
	 * @return The team of this Entity
	 */
	public TeamFlavor getTeam();
	
}
