package com.undead_pixels.dungeon_bots.file;

import java.text.ParseException;

/**
 * A fake GameEditorSection, just used for planning
 */
public class FakeSection extends GameEditorStateSection {
	private String str;

	public FakeSection(String str) {
		super();
		this.str = str;
	}

	@Override
	public String toLua() {
		return str;
	}

	@Override
	public void updateFromLuaString(String[] luaCode) throws ParseException {
		throw new ParseException("Fake class cannot update itself. It's a fake.", 0);
	}
}