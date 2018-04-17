package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

import java.util.stream.Collectors;

public class Demon extends Actor implements HasImage , Pushable {

	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Characters/Demon0.png", 2, 3);

	public Demon(World world, float x, float y) {
		super(world, "demon", DEFAULT_TEXTURE, demonScripts(), x, y);
	}

	@BindTo("new")
	@Bind(value = SecurityLevel.AUTHOR, doc = "Create a new Demon")
	public static Demon create(
			@Doc("The World the demon belongs to") LuaValue world,
			@Doc("The X position of the demon") LuaValue x,
			@Doc("The Y position of the demon") LuaValue y) {
		return new Demon(userDataOf(World.class, world), x.tofloat(), y.tofloat());
	}

	public static UserScriptCollection demonScripts() {
		 final UserScriptCollection scripts = new UserScriptCollection();
		 scripts.add(new UserScript("init", "registerBumpedListener(function(e,dir)\n" +
				 "    world:removeEntity(e)\n" +
				 "end)"));
		 return scripts;
	}

	@Override
	public LuaSandbox getSandbox() {
		final LuaSandbox sandbox = super.getSandbox();
		sandbox.registerEventType("BUMPED", "Called when the demon is bumped into", "entity", "direction");
		return sandbox;
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public String inspect() {
		return null;
	}

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	@Override
	public void push(Direction direction) {
		return;
	}

	@Override
	public void bumpedInto(final Entity e, final Actor.Direction direction) {
		getSandbox().fireEvent("BUMPED", e.getLuaValue(), LuaValue.valueOf(direction.name()));
		world.message(this, String.format("Bumped %s", direction.name().toLowerCase()), LoggingLevel.GENERAL);
	}
}
