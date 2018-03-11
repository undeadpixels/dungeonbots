package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

/**
 * Item that encapsulates opening a Browser Window
 */
public class Website extends Item {

	private final String url;

	public Website(World w, String descr, String url) {
		super(w, "Website", descr, 0, 0);
		this.url = url;
	}


	@Override
	public Boolean applyTo(Entity e) {
		if(e.getClass().equals(Player.class)) {
			this.world.openBrowser(this.url);
			return true;
		}
		return false;
	}
}
