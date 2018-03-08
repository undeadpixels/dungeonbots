package com.undead_pixels.dungeon_bots.scene.entities.items;

import com.undead_pixels.dungeon_bots.scene.World;
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


	@Override @Bind(SecurityLevel.ENTITY)
	public Boolean use() {
		this.world.openBrowser(this.url);
		return true;
	}
}
