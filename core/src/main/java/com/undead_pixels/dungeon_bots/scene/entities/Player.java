package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;

public class Player extends Actor {


	public Player(World world, String name, TextureRegion tex) {

		super(world, name, tex);
	}

	public Player(World world, String name, LuaSandbox script, TextureRegion tex) {
		super(world, name, script, tex);
	}
}
