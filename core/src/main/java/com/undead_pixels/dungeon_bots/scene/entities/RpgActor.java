package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.Weapon;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons.WeaponStats;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Stewart Charles
 * @version 1.0.1
 * @since 1.0.1
 * Prototype of an RpgActor class that has traditional RpgStats and fields.
 */
@Doc("A type that encapsulates abilities and stats for players with Rpg attributes")
public abstract class RpgActor extends Actor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int STAT_COUNT = 4;
	protected int health = 10;
	protected int mana = 10;
	protected int stamina = 10;

	// Skill stats determine the effectiveness and potency of associated skill abilities
	protected final int[] stats = new int[] { 5, 5, 5, 5 };

	public RpgActor(World world, String name, TextureRegion tex, int[] s, UserScriptCollection scripts, float x, float y) {
		super(world, name, tex, scripts, x, y);
		assert s.length == STAT_COUNT;
		System.arraycopy(s, 0, stats, 0, STAT_COUNT);
	}

	public RpgActor(World world, String name, TextureRegion tex, UserScriptCollection scripts, float x, float y) {
		super(world, name, tex, scripts, x, y);
	}

	/* -- LuaBindings -- */
	/**
	 * Returns the source players stats as a Varargs
	 * <pre>{@code
	 * str, dex, int, wis = rpg_player:stats()
	 * }</pre>
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT)
	@BindTo("stats")
	@Doc("Get the stats of the player.\n" +
			"(strength, dexterity, intelligence, wisdom) = player:getStats()")
	public Varargs getStats() {
		return  LuaValue.varargsOf(new LuaValue[] {
				LuaValue.valueOf(stats[0]),
				LuaValue.valueOf(stats[1]),
				LuaValue.valueOf(stats[2]),
				LuaValue.valueOf(stats[3])});
	}

	@Doc("Get the Strength stat of the Player")
	@Bind(SecurityLevel.DEFAULT) @BindTo("strength")
	public int getStrength() {
		return stats[0];
	}

	@Doc("Get the Dexterity stat of the Player")
	@Bind(SecurityLevel.DEFAULT) @BindTo("dexterity")
	public int getDexterity() {
		return stats[1];
	}

	@Doc("Get the Intelligence stat of the Player")
	@Bind(SecurityLevel.DEFAULT) @BindTo("intelligence")
	public int getIntelligence() {
		return stats[2];
	}

	@Doc("Get the Wisdom stat of the Player")
	@Bind(SecurityLevel.DEFAULT) @BindTo("wisdom")
	public int getWisdom() {
		return stats[3];
	}

	@Doc("Get the Health of the Player.\n" +
			"When the Players health is Zero, the player dies.")
	@Bind(SecurityLevel.DEFAULT) @BindTo("health")
	public int getHealth() {
		return health;
	}

	@Doc("Get the Mana of the Player\n" +
			"Mana is consumed when using spells, or Items that require magic power.")
	@Bind(SecurityLevel.DEFAULT) @BindTo("mana")
	public int getMana() {
		return mana;
	}

	@Doc("Get the Stamina of the Player\n" +
			"Certain actions require stamina to perform, such as attacks, or pushing heavy objects.")
	@Bind(SecurityLevel.DEFAULT) @BindTo("stamina")
	public int getStamina() {
		return stamina;
	}

	@Doc("Sets the Stats of the Player character to the specified values.")
	@Bind(SecurityLevel.AUTHOR)
	public RpgActor setStats(@Doc("A list of Integers corresponding to the players stats") Varargs v) {
		int start = v.arg1().isint() ? 1 : 2;
		assert v.narg() >= 4;
		for(int i = 0; i < STAT_COUNT; i++)
			this.stats[i] = v.arg(i + start).checkint();
		return this;
	}

	@Doc("Sets the Health of the Player to the provided value.")
	@Bind(SecurityLevel.AUTHOR)
	public RpgActor setHealth(@Doc("An Integer value greater than 0") LuaValue h) {
		this.health = h.checkint();
		return this;
	}

	@Doc("Sets the Mana of the Player to the provided value.")
	@Bind(SecurityLevel.AUTHOR)
	public RpgActor setMana(@Doc("An Integer value greater than 0") LuaValue m) {
		this.mana = m.checkint();
		return this;
	}

	@Doc("Sets the Stamina of the Player to the provided value.")
	@Bind(value=SecurityLevel.AUTHOR)
	public RpgActor setStamina(@Doc("An Integer value greater than 0") LuaValue s) {
		this.stamina = s.checkint();
		return this;
	}

	@Override
	public Boolean useItem(ItemReference ir) {
		if(ir.getItem().getClass().isAssignableFrom(Weapon.class)) {
			WeaponStats weaponStats = ((Weapon)ir.getItem()).getWeaponStats();
			this.health -= weaponStats.getDamage();
			return true;
		}
		return false;
	}

	@Override
	public Boolean canTake() {
		return this.health <= 0;
	}
}
