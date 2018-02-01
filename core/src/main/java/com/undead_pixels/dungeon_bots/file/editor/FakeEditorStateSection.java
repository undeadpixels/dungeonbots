package com.undead_pixels.dungeon_bots.file.editor;

import java.text.ParseException;

/**
 * A fake GameEditorSection, just used for planning
 */
public class FakeEditorStateSection extends GameEditorStateSection {
	private String str;

	public FakeEditorStateSection(String str) {
		super();
		this.str = str;
	}

	@Override
	public String toLua() {
		return str;
	}

	@Override
	public void updateFromLuaStrings(String[] luaCode) throws ParseException {
		throw new ParseException("Fake class cannot update itself. It's a fake.", 0);
	}
}