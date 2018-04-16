package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import org.luaj.vm2.Varargs;

public class Question extends Item {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Question(World w, String name, String descr, int value, int weight) {
		super(w, name, descr, value, weight);
	}

	public static String[] varargsToStringArr(Varargs v) {
		final String[] ans = new String[v.narg()];
		for(int i = 1; i <= v.narg(); i++) {
			ans[i - 1] = v.arg(i).tojstring();
		}
		return ans;
	}
}
