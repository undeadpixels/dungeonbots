package com.undead_pixels.dungeon_bots.scene.entities;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/** Interface representing an entity or type that has a contextual use<br>
*  method that can be invoked generically.<br>
*  <h2>Examples:</h2>
*  <em>
*  Using a switch toggles the switch.<br>
*  Using a door opens the door
*  </em>
*  */
public interface Useable {
	default Boolean use() { return false;}

	default Boolean use(final Varargs dir) {
		return false;
	}
	default Boolean give(final LuaValue dir) {
		return false;
	}
	default Boolean useSelf()   { return false; }
	default Boolean useUp()     { return false; }
	default Boolean useDown()   { return false; }
	default Boolean useLeft()   { return false; }
	default Boolean useRight()  { return false; }
	default Boolean giveUp()    { return false; }
	default Boolean giveDown()  { return false; }
	default Boolean giveLeft()  { return false; }
	default Boolean giveRight() { return false; }

}
