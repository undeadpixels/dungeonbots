package com.undead_pixels.dungeon_bots.scene.entities.inventory.items.weapons;

import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import org.luaj.vm2.LuaValue;

import java.io.Serializable;

public final class WeaponStats implements Serializable, GetLuaFacade {
	final int damage;
	final int speed;
	final int range;

	WeaponStats(int damage, int speed, int range) {
		this.damage = damage;
		this.speed = speed;
		this.range = range;
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a WeaponStats table")
	public static WeaponStats create(
			@Doc("Weapon Damage in range [0..10]") LuaValue damage,
			@Doc("Weapon Speed in range [0..10]") LuaValue speed,
			@Doc("Weapon Range in range [0..10]") LuaValue range) {
		return new WeaponStats(damage.checkint(), speed.checkint(), range.checkint());
	}

	public String getName() {
		return "Weapon Stats";
	}

	@Doc("Gets the Damage stat of the Weapon")
	@Bind(SecurityLevel.AUTHOR) @BindTo("damage")
	public Integer getDamage() {
		return this.damage;
	}

	@Doc("Gets the Speed stat of the Weapon")
	@Bind(SecurityLevel.AUTHOR) @BindTo("speed")
	public Integer getSpeed() {
		return this.speed;
	}

	@Doc("Gets the Range stat of the Weapon")
	@Bind(SecurityLevel.AUTHOR) @BindTo("range")
	public Integer getRange() {
		return this.range;
	}
}
