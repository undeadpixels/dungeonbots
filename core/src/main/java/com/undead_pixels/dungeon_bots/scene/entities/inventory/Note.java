package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

public class Note extends Item implements Useable {

	public Note(String name, String descr) {
		super("Note: " + name, descr, 0, 0);
	}

	@Bind(SecurityLevel.DEFAULT) public void read() {
		/* Create text/message window formatted and styled like
		*  a dialog window for a note. */
	}
}
