package com.undead_pixels.dungeon_bots.script.annotations;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.UserScript;

/**
 * A UserScript intended to return a boolean value. Generally, scripts that
 * return a value do not get executed in the game loop, but get executed for
 * other purposes.
 */
public class BooleanScript extends UserScript {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BooleanScript(String name, boolean defaultValue) {
		super(name);

		if (defaultValue)
			code = "return true";
		else
			code = "return false";		
	}

	public BooleanScript(String name, String code, SecurityLevel level) {
		super(name, code, level);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canExecute(World world, long time) {
		return false;
	}
}