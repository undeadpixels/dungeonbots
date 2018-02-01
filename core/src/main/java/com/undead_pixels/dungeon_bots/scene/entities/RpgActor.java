package com.undead_pixels.dungeon_bots.scene.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaSandbox;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.stream.Stream;

/**
 * @author Stewart Charles
 * @version 1.0.1
 * @since 1.0.1
 * Prototype of an RpgActor class that has traditional RpgStats and fields.
 */
public class RpgActor extends Actor implements GetLuaFacade, GetLuaSandbox {
	private final int STAT_COUNT = 4;

	// Skill resource attributes
	// The use of certain skills temporarily consumes their associated resources
	protected int health;
	protected int mana;
	protected int stamina;

	// Skill stats determine the effectiveness and potency of associated skill abilities
	protected final int[] stats = new int[STAT_COUNT];

	public RpgActor(World world, String name, TextureRegion tex) {
		super(world, name, tex);
	}

	public RpgActor(World world, String name, LuaSandbox script, TextureRegion tex) {
		super(world, name, script, tex);
	}

	/* -- LuaBindings -- */

	/**
	 * Returns the source players stats as a Varargs
	 * <pre>{@code
	 * str, dex, int, wis = rpg_player:stats()
	 * }</pre>
	 * @return
	 */
	@Bind @BindTo("stats")
	public Varargs getStats() {
		return LuaValue.varargsOf((LuaValue[])Stream.of(stats).map(CoerceJavaToLua::coerce).toArray());
	}

	@Bind @BindTo("strength")
	public int getStrength() {
		return stats[0];
	}

	@Bind @BindTo("dexterity")
	public int getDexterity() {
		return stats[1];
	}

	@Bind @BindTo("intelligence")
	public int getIntelligence() {
		return stats[2];
	}

	@Bind @BindTo("wisdom")
	public int getWisdom() {
		return stats[3];
	}

	@Bind @BindTo("health")
	public int getHealth() {
		return health;
	}

	@Bind @BindTo("mana")
	public int getMana() {
		return mana;
	}

	@Bind @BindTo("stamina")
	public int getStamina() {
		return stamina;
	}
}
