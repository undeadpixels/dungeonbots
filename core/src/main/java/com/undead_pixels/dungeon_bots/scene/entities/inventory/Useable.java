package com.undead_pixels.dungeon_bots.scene.entities.inventory;

/* Interface representing an entity or type that has a contextual use<br>
*  method that can be invoked generically.<br>
*  <em>Examples:</em><br>
*  Using a switch toggles the switch.<br>
*  Using a door opens the door
*  */
public interface Useable {
	default Boolean use()   { return false; }
	default Boolean up()    { return false; }
	default Boolean down()  { return false; }
	default Boolean left()  { return false; }
	default Boolean right() { return false; }
}
