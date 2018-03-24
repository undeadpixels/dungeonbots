package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import org.luaj.vm2.LuaValue;

import javax.swing.*;

@Doc("A Note is an Item that contains user or author provided text.")
public class Note extends Item {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Note(World world, String descr) {
		super(world,"Note", descr, 0, 0);
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.DEFAULT, doc = "Create a new Note item.")
	public static Note create(@Doc("The World the Note belongs to") LuaValue world,
							  @Doc("The text body of the Note") LuaValue descr) {
		return new Note(
				(World)world.checktable().get("this").checkuserdata(World.class),
				descr.checkjstring());
	}

	@Override
	public Boolean applyTo(Entity e) {
		/* Create text/message window formatted and styled like
		*  a dialog window for a note. */
		if(e.getClass().equals(Player.class)) {
			JOptionPane.showMessageDialog(null, this.description, this.name, JOptionPane.INFORMATION_MESSAGE);
			return true;
		}
		return false;
	}
}
