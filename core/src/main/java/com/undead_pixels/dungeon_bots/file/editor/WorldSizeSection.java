package com.undead_pixels.dungeon_bots.file.editor;

import java.text.ParseException;

/**
 * A section of a level script indicating world size
 */
public class WorldSizeSection extends LevelScriptSection {
	/**
	 * Size of this world
	 */
	public int width = 16, height = 16; // TODO - make private

	@Override
	public String toLua() {
		return "world.setSize("+width+", "+height+")";
	}

	@Override
	public void updateFromLuaStrings(String[] luaCode) throws ParseException {
		String line = luaCode[0];
		String[] vals = extractGroupsFromText("world\\.setSize\\((\\d+), (\\d+)\\)", line);
		width = intAt(vals, 0, width);
		height = intAt(vals, 1, height);
	}
	
}