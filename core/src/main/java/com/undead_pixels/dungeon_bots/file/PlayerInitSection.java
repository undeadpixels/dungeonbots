package com.undead_pixels.dungeon_bots.file;

import java.text.ParseException;

public class PlayerInitSection extends GameEditorStateSection {
	public int px = 2, py = 2; // TODO - make private

	@Override
	public String toLua() {
		return "world.setPlayer(Player.new(world, "+px+", "+py+"))";
	}

	@Override
	public void updateFromLuaString(String[] luaCode) throws ParseException {
		String line = luaCode[0];
		String[] vals = extract("world\\.setPlayer\\(Player\\.new\\(world, (\\d+), (\\d+)\\)\\)", line);
		px = intAt(vals, 0, px);
		py = intAt(vals, 1, py);
	}

}
