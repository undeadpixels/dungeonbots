package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

import javax.swing.*;

public class Note extends Item implements CanUseItem {

	public Note(World world, String name, String descr) {
		super(world,name, descr, 0, 0);
	}

	@Bind(SecurityLevel.DEFAULT) public Boolean use() {
		/* Create text/message window formatted and styled like
		*  a dialog window for a note. */
		JOptionPane.showMessageDialog(null, this.description, this.name, JOptionPane.INFORMATION_MESSAGE);
		return true;
	}
}
