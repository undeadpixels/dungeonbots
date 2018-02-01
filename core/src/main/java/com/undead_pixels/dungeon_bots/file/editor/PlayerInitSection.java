package com.undead_pixels.dungeon_bots.file.editor;

import java.text.ParseException;

import com.undead_pixels.dungeon_bots.scene.World;

/**
 * A section of a level script indicating some intialization of the player.
 * 
 * TODO - more aspects of the player should probably be configured, too
 */
public class PlayerInitSection extends LevelScriptSection {

	/**
	 * The world
	 */
	private World world;
	
	/**
	 * Location of the player
	 */
	public int px = 2, py = 2; // TODO - make private

	public PlayerInitSection(World world) {
		this.world = world;
	}

	@Override
	public String toLua() {
		return "world.setPlayer(Player.new(world, "+px+", "+py+"))";
	}

	@Override
	public void updateFromLuaStrings(String[] luaCode) throws ParseException {
		String line = luaCode[0];
		String[] vals = extractGroupsFromText("world\\.setPlayer\\(Player\\.new\\(world, (\\d+), (\\d+)\\)\\)", line);
		px = intAt(vals, 0, px);
		py = intAt(vals, 1, py);
	}

}
