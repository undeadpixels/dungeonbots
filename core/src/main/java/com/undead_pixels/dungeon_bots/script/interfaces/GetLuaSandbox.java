package com.undead_pixels.dungeon_bots.script.interfaces;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;

import org.luaj.vm2.LuaValue;

/**
 * Interface for providing a LuaSandbox for a given object
 */
public interface GetLuaSandbox {

	/**
	 * Return the LuaSandbox of this object.
	 * @return This objects LuaSandbox
	 */
	public LuaSandbox getSandbox();
	
	/**
	 * @return	All scripts associated with this object
	 */
	public UserScriptCollection getScripts();
}
