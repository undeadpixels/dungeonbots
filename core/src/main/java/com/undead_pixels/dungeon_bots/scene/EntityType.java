package com.undead_pixels.dungeon_bots.scene;


import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.script.UserScript;

/**For templating newly-placed Entities as they are added into the world.*/
public class EntityType {

	public final String name;
	public final TextureRegion texture;
	public final UserScript[] scripts;


	public EntityType(String name, TextureRegion texture, UserScript[] scripts) {
		this.name = name;
		this.texture = texture;
		this.scripts = scripts.clone();
	}


	public EntityType(String name, TextureRegion texture) {
		this.name = name;
		this.texture = texture;
		this.scripts = new UserScript[0];
	}


}
