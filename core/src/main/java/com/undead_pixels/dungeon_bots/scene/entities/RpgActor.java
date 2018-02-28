package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.State;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
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
	protected int health = 10;
	protected int mana = 10;
	protected int stamina = 10;

	// Skill stats determine the effectiveness and potency of associated skill abilities
	protected final int[] stats = new int[] { 5, 5, 5, 5 };

	public RpgActor(World world, String name, TextureRegion tex, int[] s) {
		super(world, name, tex);
		assert s.length == STAT_COUNT;
		System.arraycopy(s, 0, stats, 0, STAT_COUNT);
	}

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
	@Bind(SecurityLevel.DEFAULT) @BindTo("stats")
	public Varargs getStats() {
		return  LuaValue.varargsOf(new LuaValue[] {
				LuaValue.valueOf(stats[0]),
				LuaValue.valueOf(stats[1]),
				LuaValue.valueOf(stats[2]),
				LuaValue.valueOf(stats[3])});
	}

	@Bind(SecurityLevel.DEFAULT) @BindTo("strength")
	public int getStrength() {
		return stats[0];
	}

	@Bind(SecurityLevel.DEFAULT) @BindTo("dexterity")
	public int getDexterity() {
		return stats[1];
	}

	@Bind(SecurityLevel.DEFAULT) @BindTo("intelligence")
	public int getIntelligence() {
		return stats[2];
	}

	@Bind(SecurityLevel.DEFAULT) @BindTo("wisdom")
	public int getWisdom() {
		return stats[3];
	}

	@Bind(SecurityLevel.DEFAULT) @BindTo("health")
	public int getHealth() {
		return health;
	}

	@Bind(SecurityLevel.DEFAULT) @BindTo("mana")
	public int getMana() {
		return mana;
	}

	@Bind(SecurityLevel.DEFAULT) @BindTo("stamina")
	public int getStamina() {
		return stamina;
	}

	@Bind(SecurityLevel.AUTHOR)
	public RpgActor setStats(Varargs v) {
		int start = v.arg1().isint() ? 1 : 2;
		assert v.narg() >= 4;
		for(int i = 0; i < STAT_COUNT; i++)
			this.stats[i] = v.arg(i + start).checkint();
		return this;
	}

	@Bind(SecurityLevel.AUTHOR)
	public RpgActor setHealth(LuaValue h) {
		this.health = h.checkint();
		return this;
	}

	@Bind(SecurityLevel.AUTHOR)
	public RpgActor setMana(LuaValue m) {
		this.mana = m.checkint();
		return this;
	}

	@Bind(SecurityLevel.AUTHOR)
	public RpgActor setStamina(LuaValue s) {
		this.stamina = s.checkint();
		return this;
	}

}
